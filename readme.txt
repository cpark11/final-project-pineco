Since MS2, the following was accomplished:

For MS3 Grading, please have Location and Camera permissions enabled before running the app. 

1. LoginActivity is set to be the new default activity. Username and password are not supported in the app yet, but clicking the login button will open a WebView for the user to input their Flickr credentials and authorize
our app to have write permissions. This consumes the flickr.auth.getFrob endpoint, which provides a value that builds the authorization url. To exit the WebView and continue to the main activity, press the back button or use the multitasking button to go back to FlickrTron.
2. The single search bar in MainActivity allows the user to search for photos with tags matching the query, which consumes the flickr.photos.search endpoint.
3. If the user would like to search for photos taken nearby, they can tap the location icon next to the search bar, which requests the current GPS location, and gets the name of the location using the
flickr.places.findByLatLon endpoint. The resulting location string is then passed into the search bar.
