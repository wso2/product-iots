function changeImage() {
    var image = document.getElementById('myImage');
    if (image.src.match("bulb-on")) {

        image.src = "/iot/public/mydevice/images/bulb-off.png";
    } else {

        image.src = "/iot/public/mydevice/images/bulb-on.png";
    }
}