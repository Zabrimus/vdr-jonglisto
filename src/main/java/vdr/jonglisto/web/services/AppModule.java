package vdr.jonglisto.web.services;

import java.io.IOException;

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
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestGlobals;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.pageload.PreloaderMode;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import vdr.jonglisto.lib.CommandService;
import vdr.jonglisto.lib.ConfigurationService;
import vdr.jonglisto.lib.EpgDataService;
import vdr.jonglisto.lib.EpgImageService;
import vdr.jonglisto.lib.SearchTimerService;
import vdr.jonglisto.lib.SvdrpNashornService;
import vdr.jonglisto.lib.VdrDataService;
import vdr.jonglisto.lib.impl.CommandServiceImpl;
import vdr.jonglisto.lib.impl.ConfigurationServiceImpl;
import vdr.jonglisto.lib.impl.EpgDataServiceImpl;
import vdr.jonglisto.lib.impl.EpgImageServiceImpl;
import vdr.jonglisto.lib.impl.SearchTimerServiceImpl;
import vdr.jonglisto.lib.impl.SvdrpNashornServiceImpl;
import vdr.jonglisto.lib.impl.VdrDataServiceImpl;
import vdr.jonglisto.lib.model.EPGMedia;
import vdr.jonglisto.lib.util.Constants;
import vdr.jonglisto.web.binding.MapBindingFactory;
import vdr.jonglisto.web.encoder.ChannelEncoder;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
public class AppModule {

    private static ObjectMapper mapper = new ObjectMapper();

    public static void bind(ServiceBinder binder) {
        binder.bind(ConfigurationService.class, ConfigurationServiceImpl.class);
        binder.bind(VdrDataService.class, VdrDataServiceImpl.class);
        binder.bind(EpgDataService.class, EpgDataServiceImpl.class);
        binder.bind(EpgImageService.class, EpgImageServiceImpl.class);
        binder.bind(CommandService.class, CommandServiceImpl.class);
        binder.bind(SearchTimerService.class, SearchTimerServiceImpl.class);
        binder.bind(SvdrpNashornService.class, SvdrpNashornServiceImpl.class);
        binder.bind(ChannelEncoder.class);
        binder.bind(GlobalLogoFilename.class);
        binder.bind(GlobalValues.class);
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        configuration.override(SymbolConstants.APPLICATION_VERSION, Constants.version);
        configuration.override(SymbolConstants.PRODUCTION_MODE, false);
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

        configuration.add(new CoercionTuple<String, EPGMedia>(String.class, EPGMedia.class, coercion1));
        configuration.add(new CoercionTuple<EPGMedia, String>(EPGMedia.class, String.class, coercion2));
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void setupEnvironment(MappedConfiguration<String, Object> configuration) {
        configuration.override(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
        configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:jbootstrap");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "random value!");

        configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
        configuration.add(SymbolConstants.ENABLE_HTML5_SUPPORT, true);
        configuration.add(SymbolConstants.ENABLE_PAGELOADING_MASK, true);
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "de");
        configuration.add(SymbolConstants.CLUSTERED_SESSIONS, false);
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, true);
        configuration.add(SymbolConstants.COMPACT_JSON, true);
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, true);
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, true);
        configuration.add(SymbolConstants.PRELOADER_MODE, PreloaderMode.ALWAYS);

        // configuration.add("tapestry.closure-compiler-level",
        // "WHITESPACE_ONLY");
        configuration.add("tapestry.closure-compiler-level", "SIMPLE_OPTIMIZATIONS"); // <--
        // configuration.add("tapestry.closure-compiler-level",
        // "ADVANCED_OPTIMIZATIONS"); // INFO: do not use this!

        // INFO:
        // only in production 1 to 5 minutes
        // configuration.add(SymbolConstants.FILE_CHECK_INTERVAL, 60);

        // INFO:
        // conversation is the desired default strategy, but only if the
        // conversation moderator works again for Tapestry 5.4.1
        // otherwise some persistent page data will be deleted too often and too
        // early
        // configuration.add(SymbolConstants.PERSISTENCE_STRATEGY,
        // "conversation");
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

    @Startup
    public static void initApplication(RegistryShutdownHub shutdownHub, ConfigurationService service,
            ComponentClassResolver componentClassResolver, ComponentSource componentSource) {

        shutdownHub.addRegistryShutdownListener(new Runnable() {

            public void run() {
                service.shutdown();
            }
        });

        service.triggerInitialization();

        componentClassResolver.getPageNames().stream().forEach(s -> {
            // preload all pages
            componentSource.getPage(s);
        });
    }
}
