//
// Entry point for OSD entry list 
//
// Output: 
//   Array of
//      result.display       display name of the osd entry
//      result.sql           the sql statement which shall be executed
//
var getOsd = function(input) {
    
    var result = [
           {
               "display" : "Show merge",
               "sql"     : "select count(source) as '1', source as '2' from events group by source"
           },
           
           {
               "display" : "Nachrichten",
               "sql"     : "select DATE_FORMAT(FROM_UNIXTIME(inssp), '%d.%m.%Y') as '1', DATE_FORMAT(FROM_UNIXTIME(inssp), '%H:%i') as '2', title as '3', state as '4', text as '5' from messages order by inssp desc limit 50"
           },
           
           {
               "display" : "Erledigte Timer",
               "sql"     : "select DATE_FORMAT(FROM_UNIXTIME(inssp), '%d.%m.%Y') as '1', DATE_FORMAT(FROM_UNIXTIME(starttime), '%H:%i') as '2', episodeseason as '3', episodepart as '4', title as '5', shorttext as '6', channelname as '7' from  timersdone where state <> 'D' order by starttime desc"
           }
        ]

    return result;
}
