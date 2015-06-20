uint8_t buffer[BUFFER_SIZE + 1];
int bufindex = 0;
char action[MAX_ACTION + 1];
char path[MAX_PATH + 1];

void readControls() {
    // Try to get a client which is connected.
    Adafruit_CC3000_ClientRef client = httpServer.available();
    if (client) {

        bufindex = 0;
        memset(&buffer, 0, sizeof(buffer));

        // Clear action and path strings.
        memset(&action, 0, sizeof(action));
        memset(&path, 0, sizeof(path));

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

        wdt_reset();

        if (parsed) {
            if (strcmp(action, "GET") == 0) {
                responseMsg = path;

                if (DEBUG) {
                    Serial.println(responseMsg);
                }

                int index = responseMsg.lastIndexOf("/");
                int newLine = responseMsg.indexOf("/");
                subStrn = responseMsg.substring(index + 1);
                responseMsg = responseMsg.substring(newLine + 1, index);

                if (DEBUG) {
                    Serial.print(responseMsg);
                    Serial.print(" - ");
                    Serial.println(subStrn);
                    Serial.println("-------------------------------");
                }
            }

            if (responseMsg == "TEMP") {
                int temperature = (uint8_t) getTemperature();

                client.fastrprintln(F("HTTP/1.1 200 OK"));
                client.fastrprintln(F("Connection: close"));
                client.fastrprintln(F(""));
                client.fastrprint(String(temperature).c_str());
            }
        }

        delay(100);

        // Close the connection when done.
        Serial.println(F("Client disconnected"));
        client.close();
        wdt_reset();

    }
}

bool parseRequest(uint8_t *buf, int bufSize, char *action, char *path) {
    // Check if the request ends with \r\n to signal end of first line.
    if (bufSize < 2)
        return false;
    if (buf[bufSize - 2] == '\r' && buf[bufSize - 1] == '\n') {
        parseFirstLine((char *) buf, action, path);
        return true;
    }
    return false;
}

// Parse the action and path from the first line of an HTTP request.
void parseFirstLine(char *line, char *action, char *path) {
    // Parse first word up to whitespace as action.
    char *lineaction = strtok(line, " ");
    if (lineaction != NULL)
        strncpy(action, lineaction, MAX_ACTION);
    // Parse second word up to whitespace as path.
    char *linepath = strtok(NULL, " ");
    if (linepath != NULL)
        strncpy(path, linepath, MAX_PATH);
}
