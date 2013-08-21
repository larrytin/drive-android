 
function setLocalFlashFilePath(){
 

var file_path = window.CallJava.getFlashFilePath();
var obj = document.getElementById("mov");
obj.innerHTML = "<embed src=\""+ file_path +"\" quality=\"high\" bgcolor=\"#ffffff\" width=\"550\" height=\"440\" swLiveConnect=true id=\"StarsChart\" name=\"StarsChart\" align=\"middle\" allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" />";
     
    
    
    
    
    
}