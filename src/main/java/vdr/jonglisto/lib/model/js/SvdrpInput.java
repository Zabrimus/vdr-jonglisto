package vdr.jonglisto.lib.model.js;

public class SvdrpInput {

    private String host;
    private String svdrp_command;
    private String svdrp_param;
    private String timer_aux;
    private String timer_filename;
    private String response_line;

    private boolean filter;
    private String route_to_alias;

    public SvdrpInput() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSvdrp_command() {
        return svdrp_command;
    }

    public void setSvdrp_command(String svdrp_command) {
        this.svdrp_command = svdrp_command;
    }

    public String getSvdrp_param() {
        return svdrp_param;
    }

    public void setSvdrp_param(String svdrp_param) {
        this.svdrp_param = svdrp_param;
    }

    public String getTimer_aux() {
        return timer_aux;
    }

    public void setTimer_aux(String timer_aux) {
        this.timer_aux = timer_aux;
    }

    public String getTimer_filename() {
        return timer_filename;
    }

    public void setTimer_filename(String timer_filename) {
        this.timer_filename = timer_filename;
    }

    public String getResponse_line() {
        return response_line;
    }

    public void setResponse_line(String response_line) {
        this.response_line = response_line;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public String getRoute_to_alias() {
        return route_to_alias;
    }

    public void setRoute_to_alias(String route_to_alias) {
        this.route_to_alias = route_to_alias;
    }

    @Override
    public String toString() {
        return "Timer [host=" + host + ", svdrp_command=" + svdrp_command + ", svdrp_param=" + svdrp_param
                + ", timer_aux=" + timer_aux + ", timer_filename=" + timer_filename + ", response_line=" + response_line
                + ", filter=" + filter + ", route_to_alias=" + route_to_alias + "]";
    }

}
