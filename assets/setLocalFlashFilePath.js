 
function setLocalFlashFilePath(){
	var file_path = window.CallJava.getFlashFilePath();
	var obj = document.getElementById("mov");
	obj.innerHTML += "<embed src=\""+ file_path +"\" quality=\"high\" bgcolor=\"#ffffff\" width=\"750\" height=\"450\" swLiveConnect=true name=\"mov\" align=\"middle\" allowScriptAccess=\"sameDomain\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" />";
}

function getFlash(movieName){  
   if (window.document[movieName]){  
       return window.document[movieName];  
   }  

   if (navigator.appName.indexOf("Microsoft Internet")==-1){  
     if (document.embeds && document.embeds[movieName])  
       return document.embeds[movieName];  
   } else{  
     return document.getElementById(movieName);  
   }  
} 

function stopFlash(){
	var flash = getFlash("mov");
	flash.StopPlay();
} 

function startFlash(){
	var flash = getFlash("mov");
	flash.Play();
} 