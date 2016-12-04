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

    
    // don't change any timer value and route everything to stream1
    result.route_to_alias = "stream1";
    
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
       
    // route everything to stream1
    result.route_to_alias = "stream1";
     
    return result;
}