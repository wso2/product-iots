uint8_t buffer[BUFFER_SIZE+1];
int bufindex = 0;
char action[MAX_ACTION+1];
char path[MAX_PATH+1];

void readControls()
{
  
  // Try to get a client which is connected.
  Adafruit_CC3000_ClientRef client = httpServer.available();
   if(client){
    bufindex = 0;
    memset(&buffer, 0, sizeof(buffer));
    
    // Clear action and path strings.
    memset(&action, 0, sizeof(action));
    memset(&path,   0, sizeof(path));

    // Set a timeout for reading all the incoming data.
    unsigned long endtime = millis() + TIMEOUT_MS;
    
    // Read all the incoming data until it can be parsed or the timeout expires.
    bool parsed = false;
    while (!parsed && (millis() < endtime) && (bufindex < BUFFER_SIZE)) {
      if (client.available()) {
        buffer[bufindex++] = client.read();
      }
      parsed = parseRequest(buffer, bufindex, action, path);
    }

    if (parsed) {
//      Serial.print(F("Path: ")); Serial.println(path);

      if (strcmp(action, "GET") == 0) {
          urlPath = path;
          urlPath.replace("/move/","");
          urlPath.replace("/","");

          if(urlPath.endsWith("F")){
            updateDirectionVariable(1);
            
          }else if(urlPath.endsWith("B")){    
            updateDirectionVariable(2);
            
          }else if(urlPath.endsWith("L")){      
            updateDirectionVariable(3);
            
          }else if(urlPath.endsWith("R")){         
            updateDirectionVariable(4);
            
          }else if(urlPath.endsWith("S")){      
            updateDirectionVariable(5);
          }    
      }  
    } 

    // Wait a short period to make sure the response had time to send before
    // the connection is closed (the CC3000 sends data asyncronously).
    delay(200);
    client.flush();
    client.close();
 
   }
  
}



bool parseRequest(uint8_t* buf, int bufSize, char* action, char* path) {
  // Check if the request ends with \r\n to signal end of first line.
  if (bufSize < 2)
    return false;
  if (buf[bufSize-2] == '\r' && buf[bufSize-1] == '\n') {
    parseFirstLine((char*)buf, action, path);
    return true;
  }
  return false;
}

// Parse the action and path from the first line of an HTTP request.
void parseFirstLine(char* line, char* action, char* path) {
  // Parse first word up to whitespace as action.
  char* lineaction = strtok(line, " ");
  if (lineaction != NULL)
    strncpy(action, lineaction, MAX_ACTION);
  // Parse second word up to whitespace as path.
  char* linepath = strtok(NULL, " ");
  if (linepath != NULL)
    strncpy(path, linepath, MAX_PATH);
}
