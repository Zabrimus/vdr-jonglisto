//
// Entry point for filter 
//
// Input:
//   input.host              client hostname
//   input.svdrp_command     Current svdrp command
//   input.svdrp_param       Whole parameter list of the svdrp command
//   input.response_line     One response line of the the response
// 
// Output:
//   filter                   True (accepted) or false (rejected)
//
var filter = function(input) {
    
    var result = {
            "filter" : true,
        };

    return result;
}


//
// Entry point for timer commands: LSTT, DELT, MODT, NEWT
//
// Input:
//   input.host              client hostname
//   input.svdrp_command     Current svdrp command
//   input.svdrp_param       Whole parameter list of the svdrp command
//   input.timer_aux         Current timer aux value if it exists
//   input.timer_filename    Current timer filename if it exists
// 
// Output:
//   route_to_alias          Route the current svdrp command to the defined alias
//   timer_aux               Replace timer aux with this new version
//   timer_filename          Replace timer filename with this new version
//
var preprocessTimer = function(input) {
    
    var result = {
            "route_to_alias" : null,
            "timer_aux" : null,
            "timer_filename" : null
        };
    
    var remotetimer_tag = "";

    // routing
    switch (input.host) {
    case "pivdr1":
    case "pivdr1.lan":
        result.route_to_alias = "stream1";
        remotetimer_tag = "<remotetimers>1</remotetimers>";
        break;

    case "pivdr2":
    case "pivdr2.lan":
        result.route_to_alias = "stream2";
        result.timer_filename = input.host + "~" + input.timer_filename;
        remotetimer_tag = "<remotetimers>2</remotetimers>";
        break;

    default:
        // don't route timer request
        result.route_to_alias = null;
        break;
    }

    // modify aux for creation or modify
    if ((input.svdrp_command = "MODT") || (input.svdrp_command = "NEWT")) {
        result.timer_aux = input.timer_aux;

        // append remotetimers tag if it not exists
        if (!/<remotetimers>.*?<\/remotetimers>/g.exec(input.aux)) {
            result.timer_aux = result.timer_aux + remotetimer_tag;
        }

        // append jonglisto tag if it not exists
        if (!/<jonglisto>.*?<\/jonglisto>/g.exec(input.aux)) {
            result.timer_aux = result.timer_aux + "<jonglisto><from>"
                    + input.host + "</from><to>" + result.route_to_alias
                    + "</to></jonglisto>";
        }

        // check filename
        if (!input.timer_filename.startsWith(input.host + "~")) {
            result.timer_filename = input.host + "~" + input.timer_filename;
        }
    }

    return result;
}


//
// Entry point for common svdrp commands
//
// Input: 
//   input.host client hostname 
//   input.svdrp_command Current svdrp command
//   input.svdrp_param Whole parameter list of the svdrp command
// 
// Output: route_to_alias the configured alias which shall receive this command,
//                        or null if no routing is desired
//
var route = function(input) {
    
    var result = {
            "route_to_alias" : null
        }
       
    switch (input.svdrp_command) {
    case "LSTT":
    case "DELT":
    case "MODT":
    case "NEWT":

        // route timer requests
        switch (input.host) {
        case "pivdr1":
        case "pivdr1.lan":
            result.route_to_alias = "stream1";
            break;

        case "pivdr2":
        case "pivdr2.lan":
            result.route_to_alias = "stream2";
            break;
        }

        break;
    }

    return result;
}