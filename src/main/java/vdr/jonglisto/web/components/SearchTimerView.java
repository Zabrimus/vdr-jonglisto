package vdr.jonglisto.web.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;

import vdr.jonglisto.lib.model.Channel;
import vdr.jonglisto.lib.model.SearchTimer;
import vdr.jonglisto.lib.model.VDR;
import vdr.jonglisto.lib.util.Function;
import vdr.jonglisto.web.encoder.VDREncoder;
import vdr.jonglisto.web.services.GlobalValues;

public class SearchTimerView extends BaseComponent {

    @Inject
    protected BeanModelSource beanModelSource;

    @Inject
    protected Messages messages;

    @Inject
    private Request request;

    @Inject
    SelectModelFactory selectModelFactory;

    @Inject
    private GlobalValues globalValues;

    @InjectComponent
    private Zone searchTimerListZone;

    @Inject
    private Block viewBlock;

    @Inject
    private Block editBlock;

    @Inject
    private Block epgBlock;

    @Property
    private BeanModel<Object> searchTimerModel;

    @Property
    private List<SearchTimer> searchTimers;

    @Persist
    @Property
    private SearchTimer searchTimer;

    @Persist
    private Long searchTimerId;

    @Property
    private String currentChannel;

    @Persist
    @Property
    private Function function;

    @Property
    @Persist
    private List<Channel> selectedChannels;

    @Property
    @Persist
    private List<String> selectedCategories;

    @Persist
    @Property
    private StringValueEncoder categoryEncoder;

    @Persist
    @Property
    private SelectModel categoryModel;

    @Property
    @Persist
    private List<String> selectedGenres;

    @Persist
    @Property
    private StringValueEncoder genreEncoder;

    @Persist
    @Property
    private VDREncoder vdrEncoder;

    @Persist
    @Property
    private SelectModel genreModel;

    @Persist
    @Property
    private SelectModel vdrModel;

    @Persist
    @Property
    private VDR selectedVdr;

    @Persist
    @Property
    List<Map<String, Object>> searchResult;

    void setupRender() {
        String reset = request.getParameter("reset");

        if ((function == null) || ((reset != null) && "true".equals(reset))) {
            function = Function.LIST;
        }

        searchTimers = searchTimerService.getSearchTimers();

        categoryModel = selectModelFactory.create(globalValues.getCategories());
        categoryEncoder = new StringValueEncoder();
        selectedCategories = new ArrayList<>();

        genreModel = selectModelFactory.create(globalValues.getGenres());
        genreEncoder = new StringValueEncoder();
        selectedGenres = new ArrayList<>();

        List<VDR> v = configuration.getSortedVdrList();
        vdrModel = selectModelFactory.create(v, "displayName");
        vdrEncoder = new VDREncoder(v);
    }

    public void afterRender() {        
    }

    public void onToggleSearchTimerActive(Long id) {
        searchTimerService.toggleActive(id);

        searchTimers = searchTimerService.getSearchTimers();

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onExecuteSearchTimer(Long id) {
        function = Function.EPG;

        searchResult = searchTimerService.performSearch(searchTimerService.getSearchTimer(id));

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onBack() {
        function = Function.LIST;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onEditSearchTimer(Long id) {
        function = Function.EDIT;

        searchTimer = searchTimerService.getSearchTimer(id);
        searchTimerId = id;

        selectedChannels = new ArrayList<>();
        if (searchTimer.getChannelsList() != null) {
            searchTimer.getChannelsList().stream().forEach(s -> selectedChannels
                    .add(vdrDataService.getChannel(currentVdrView.getChannelVdr().get(), s).get()));
        }

        selectedCategories = new ArrayList<>();
        if (searchTimer.getCategory() != null) {
            searchTimer.getCategory().stream().forEach(s -> selectedCategories.add(s));
        }

        selectedGenres = new ArrayList<>();
        if (searchTimer.getGenre() != null) {
            searchTimer.getGenre().stream().forEach(s -> selectedGenres.add(s));
        }

        selectedVdr = configuration.getVdr(searchTimer.getVdrUuid());

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onDeleteSearchTimer(Long id) {
        searchTimerService.deleteSearchTimer(id);

        searchTimers = searchTimerService.getSearchTimers();

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onNewSearchTimer() {
        function = Function.EDIT;
        searchTimer = new SearchTimer();
        searchTimerId = null;

        selectedChannels = new ArrayList<>();
        selectedCategories = new ArrayList<>();
        selectedGenres = new ArrayList<>();
        selectedVdr = null;
        searchTimer.setNamingMode(1);
        searchTimer.setPriority(50);
        searchTimer.setLifetime(99);
        searchTimer.setActive(true);

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public void onPrepareForSubmit() {
        if (searchTimerId != null) {
            searchTimer = searchTimerService.getSearchTimer(searchTimerId);
        } else {
            searchTimer = new SearchTimer();

            // default values
            searchTimer.setChannelExclude(false);
            searchTimer.setCasesensitiv(false);
            searchTimer.setNoepgmatch(true);
            searchTimer.setSource("webif");
            searchTimer.setNamingMode(1);
        }
    }

    void onFailure() {
    }

    void onSuccess() {
        // set channels, genre and category
        searchTimer.setChannelsList(selectedChannels.stream().map(s -> s.getId()).collect(Collectors.toList()));
        searchTimer.setGenre(selectedGenres);
        searchTimer.setCategory(selectedCategories);

        if (selectedVdr != null) {
            searchTimer.setVdrUuid(selectedVdr.getUuid());
        } else {
            searchTimer.setVdrUuid("any");
        }

        // Create or update timer
        if (searchTimer.getId() == null) {
            // create timer
            searchTimerService.insertSearchTimer(searchTimer);
        } else {
            // update timer
            searchTimerService.updateSearchTimer(searchTimer);
        }

        function = Function.LIST;
        searchTimer = null;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }

        searchTimers = searchTimerService.getSearchTimers();
    }

    void onCancel() {
        function = Function.LIST;
        searchTimerId = null;

        if (request.isXHR()) {
            ajaxResponseRenderer.addRender(searchTimerListZone);
        }
    }

    public String getTimerAction() {
        switch (searchTimer.getType()) {
        case "R":
            return messages.get("timer_type_record");
        case "V":
            return messages.get("timer_type_switch");
        case "S":
            return messages.get("timer_type_search");
        default:
            return messages.get("timer_type_unknown");            
        }
    }
    
    public void setTimerAction(String s) {
    }

    public List<String> getChannels() {
        return searchTimer.getChannelsList().stream() //
                .map(ch -> vdrDataService.getChannel(getTimerUuid(), ch)) //
                .map(ch -> ch.orElse(Channel.emptyChannel).getName()) //
                .collect(Collectors.toList());
    }

    public String getCurrentVdrName() {
        return configuration.getVdr(searchTimer.getVdrUuid()).getDisplayName();
    }

    public Object getActiveBlock() {
        switch (function) {
        case EDIT:
            return editBlock;

        case LIST:
            return viewBlock;

        case EPG:
            return epgBlock;

        default:
            return viewBlock;
        }
    }
}
