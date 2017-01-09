package vdr.jonglisto.web.services;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.StackExtension;
import org.apache.tapestry5.services.pageload.PreloaderMode;
import org.slf4j.Logger;
import org.tynamo.security.services.SecurityFilterChainFactory;
import org.tynamo.security.services.impl.SecurityFilterChain;

import com.fasterxml.jackson.databind.ObjectMapper;

import vdr.jonglisto.lib.ChannelMapService;
import vdr.jonglisto.lib.CommandService;
import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.Epg2VdrNashornService;
import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.EpgdSearchTimerService;
import vdr.jonglisto.lib.SvdrpNashornService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.impl.ChannelMapServiceImpl;
import vdr.jonglisto.lib.impl.CommandServiceImpl;
import vdr.jonglisto.lib.impl.ConfigurationServiceImpl;
import vdr.jonglisto.lib.impl.Epg2VdrNashornServiceImpl;
import vdr.jonglisto.lib.impl.EpgDataServiceFacadeImpl;
import vdr.jonglisto.lib.impl.EpgImageServiceFacadeImpl;
import vdr.jonglisto.lib.impl.EpgdSearchTimerServiceImpl;
import vdr.jonglisto.lib.impl.SvdrpNashornServiceImpl;
import vdr.jonglisto.lib.impl.VdrDataServiceImpl;
import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.util.Constants;
import vdr.jonglisto.web.binding.MapBindingFactory;
import vdr.jonglisto.web.encoder.ChannelEncoder;
import vdr.jonglisto.web.model.User;
import vdr.jonglisto.web.realm.JdbcSaltedRealm;
import vdr.jonglisto.web.services.security.UserService;
import vdr.jonglisto.web.services.security.impl.UserServiceImpl;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
public class AppModule {

    private static ObjectMapper mapper = new ObjectMapper();

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public static void bind(ServiceBinder binder) {
        binder.bind(RandomNumberGenerator.class, SecureRandomNumberGenerator.class);
        binder.bind(ConfigurationService.class, ConfigurationServiceImpl.class);
        binder.bind(VdrDataService.class, VdrDataServiceImpl.class);
        binder.bind(EpgDataService.class, EpgDataServiceFacadeImpl.class);
        binder.bind(EpgImageService.class, EpgImageServiceFacadeImpl.class);
        binder.bind(CommandService.class, CommandServiceImpl.class);
        binder.bind(EpgdSearchTimerService.class, EpgdSearchTimerServiceImpl.class);
        binder.bind(SvdrpNashornService.class, SvdrpNashornServiceImpl.class);
        binder.bind(Epg2VdrNashornService.class, Epg2VdrNashornServiceImpl.class);
        binder.bind(ChannelMapService.class, ChannelMapServiceImpl.class);
        binder.bind(UserService.class, UserServiceImpl.class);
        binder.bind(ChannelEncoder.class);
        binder.bind(GlobalLogoFilename.class);
        binder.bind(GlobalValues.class);
    }

    @Contribute(JavaScriptStack.class)
    @Core
    public static void overrideJQueryWithNewerVersion(final OrderedConfiguration<StackExtension> configuration) {
        configuration.override("jquery-library", StackExtension.library("webjars:jquery:$version/dist/jquery.js"));
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        configuration.override(SymbolConstants.APPLICATION_VERSION, Constants.version);
        configuration.override(SymbolConstants.PRODUCTION_MODE, false);
        // configuration.override(SymbolConstants.PRODUCTION_MODE, true);
    }

    public void contributeRequestHandler(OrderedConfiguration<RequestFilter> configuration,
            @InjectService("TimingFilter") final RequestFilter timingFilter,
            @InjectService("Utf8Filter") final RequestFilter utf8Filter) {
        configuration.add("Timing", timingFilter);
        configuration.add("Utf8Filter", utf8Filter);
    }

    public static void contributeBindingSource(MappedConfiguration<String, BindingFactory> config) {
        config.addInstance("map", MapBindingFactory.class);
    }

    public static void contributeWebSecurityManager(Configuration<Realm> configuration, AuthorizingRealm realm) {
        configuration.add(realm);
    }    

    public static void contributeSecurityConfiguration(Configuration<SecurityFilterChain> configuration, SecurityFilterChainFactory factory) {
        // login page
        configuration.add(factory.createChain("/signin").add(factory.anon()).build());
        configuration.add(factory.createChain("/").add(factory.authc()).build());
        
        // permissions for all pages
        configuration.add(factory.createChain("/channelconfig/**").add(factory.perms(), "page:channelconfig").build());
        configuration.add(factory.createChain("/channelmap/**").add(factory.perms(), "page:channelmap").build());
        configuration.add(factory.createChain("/index/**").add(factory.perms(), "page:index").build());
        configuration.add(factory.createChain("/programchannel/**").add(factory.perms(), "page:programchannel").build());
        configuration.add(factory.createChain("/programday/**").add(factory.perms(), "page:programday").build());
        configuration.add(factory.createChain("/programtime/**").add(factory.perms(), "page:programtime").build());
        configuration.add(factory.createChain("/recordings/**").add(factory.perms(), "page:recordings").build());
        configuration.add(factory.createChain("/searchtimer/**").add(factory.perms(), "page:searchtimer").build());
        configuration.add(factory.createChain("/setup/**").add(factory.perms(), "page:setup").build());
        configuration.add(factory.createChain("/svdrpconsole/**").add(factory.perms(), "page:svdrpconsole").build());
        configuration.add(factory.createChain("/timer/**").add(factory.perms(), "page:timer").build());        
    }

    public static void contributeTypeCoercer(Configuration<CoercionTuple<?, ?>> configuration) {
        Coercion<String, EPGMedia> coercion1 = new Coercion<String, EPGMedia>() {

            public EPGMedia coerce(String input) {
                try {
                    return mapper.readValue(input, EPGMedia.class);
                } catch (IOException e) {
                    // must not happen
                    e.printStackTrace();
                    return null;
                }
            }
        };

        configuration.add(new CoercionTuple<String, EPGMedia>(String.class, EPGMedia.class, coercion1));

        Coercion<EPGMedia, String> coercion2 = new Coercion<EPGMedia, String>() {

            public String coerce(EPGMedia input) {
                try {
                    return mapper.writeValueAsString(input);
                } catch (IOException e) {
                    // must not happen
                    e.printStackTrace();
                    return null;
                }
            }
        };

        configuration.add(new CoercionTuple<EPGMedia, String>(EPGMedia.class, String.class, coercion2));

        Coercion<String, Channel> coercion3 = new Coercion<String, Channel>() {

            public Channel coerce(String input) {
                Channel ch = new Channel();
                ch.setId(input);
                return ch;
            }
        };

        configuration.add(new CoercionTuple<String, Channel>(String.class, Channel.class, coercion3));
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void setupEnvironment(MappedConfiguration<String, Object> configuration) {
        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
        configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:jbootstrap");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "random value!");

        configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
        configuration.add(SymbolConstants.ENABLE_HTML5_SUPPORT, true);
        configuration.add(SymbolConstants.ENABLE_PAGELOADING_MASK, true);
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "de,en");
        configuration.add(SymbolConstants.CLUSTERED_SESSIONS, false);
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, true);
        configuration.add(SymbolConstants.COMPACT_JSON, true);
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, true);
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, true);
        configuration.add(SymbolConstants.PRELOADER_MODE, PreloaderMode.ALWAYS);

        configuration.add("tapestry.closure-compiler-level", "SIMPLE_OPTIMIZATIONS"); 

        // INFO:
        // only in production 1 to 5 minutes
        // configuration.add(SymbolConstants.FILE_CHECK_INTERVAL, 60);
    }

    public RequestFilter buildTimingFilter(final Logger log) {
        return new RequestFilter() {

            public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
                long startTime = System.currentTimeMillis();

                try {
                    return handler.service(request, response);
                } finally {
                    long elapsed = System.currentTimeMillis() - startTime;

                    log.info(String.format("Request time: %d ms, %s", elapsed, request.getPath()));
                }
            }
        };
    }

    public RequestFilter buildUtf8Filter(@InjectService("RequestGlobals") final RequestGlobals requestGlobals,
            final Logger log) {
        return new RequestFilter() {

            public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
                requestGlobals.getHTTPServletRequest().setCharacterEncoding("UTF-8");
                return handler.service(request, response);
            }
        };
    }

    public AuthorizingRealm buildRealm(UserService userService, ConfigurationService configService) {
        JdbcSaltedRealm realm = new JdbcSaltedRealm(userService, configService);
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher("SHA-256");
        matcher.setHashIterations(1024);
        matcher.setStoredCredentialsHexEncoded(false);
        realm.setCredentialsMatcher(matcher);

        return realm;
    }

    @Startup
    public static void initApplication(RegistryShutdownHub shutdownHub, ConfigurationService service, EpgDataService epgService, UserService userService,
            ComponentClassResolver componentClassResolver, ComponentSource componentSource) {

        shutdownHub.addRegistryShutdownListener(new Runnable() {

            public void run() {
                service.shutdown();
            }
        });

        service.triggerInitialization();

        if (!service.isUseEpgd() && (service.getEpgVdrUuuid() != null)) {
            // Update database periodically (2 times a day)
            scheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    epgService.updateInternalEpgData(service.getEpgVdrUuuid());
                }
            }, 0, 12, TimeUnit.HOURS);
        }
        
        // create default admin user, if he/she does not exists
        if (!userService.existsUser("admin")) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword("jonglisto");
            user = userService.createUser(user);
                        
            // set default permission. Permission to do everything
            userService.addIndividualPermission(user.getId(), "*", null);
        }
        
        componentClassResolver.getPageNames().stream().forEach(s -> {
            // preload all pages
            componentSource.getPage(s);
        });
    }
}
