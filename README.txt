This is an prototype android (API 27 Oreo +) application for borrowing and returning tools from the FabLab. Users can sign in using their student ID and email in order to gain access to a rack, connected through firebase and an RPi. The user can then lock and unlock the rack while adding and removing the tools from the rack, detected through a weight sensor which determines availability and sends the databasek to Firebase for the app to display. Users can poke other users to prompt them to return the tool they have borrowed. Admins have special priviliges which allow them to access the history as well as edit the configuration of the tools as well as the racks.

The google-services.json has been added to the .gitignore to prevent random access to the actual database. 


