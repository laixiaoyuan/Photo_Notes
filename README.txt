*****************
*               *
*  Photo Notes  *
*               *
*****************Developed by Xiaoyuan Lai, in May 2016 for the class COEN268 project 4.This app is a note-taking app that allows you to take a photo, draw on the photo, record voice note, write a caption, and record the location for picture taken
_____________________________________________________________________________________

If run in emulator, need to enable Webcam option, and have Internet connection.
If run in device, need to have Internet connection, turn on Google Location service.
_____________________________________________________________________________________

In the Main activity, it displays all pictures with notes if available.
It has a Option Menu that has an “ADD” option and an “Uninstall” that only shows in overflow menu. It also has a floating action button to add one note.
_____________________________________________________________________________________

In the Add Photo activity, it allows text caption, record/re-record and playback voice note, photo taking with camera, draw on the preview of photo, shake phone to erase the drawing, record current location for picture taken using the last known location, and save the edited picture.
Photo preview is displayed using custom view. And it is scaled properly to fit the size of custom view.
It has a floating action button, when clicked, it goes back to the top of the page.
_____________________________________________________________________________________

In View Photo activity, it displays the edited photo and caption. It also has a play button to playback voice note. And a map button to launch Map View Activity.
_____________________________________________________________________________________

In Map View activity, it uses SupportMapFragment. A marker is set to indicate the location of picture taken, and it is centered. Zoom level 17 used._____________________________________________________________________________________This app has one SQLite database, and uses CursorAdaptor to link database and ListView.Image and audio files are stored under /storage/emulated/0
Thumbnails used in the main activityThe “ADD” button goes to “Add Photo” activity first before it goes to camera picture captureImpletemented 6.0 run-time permission request and set also set targetSDK to 22 in build.gradle fileSupport both landscape mode and portrait mode

Current location obtained by using the last known location

