function refreshSwatch() {
    var intensity = $( "#bulb-intensity" ).slider( "value" );
    $('#light-bulb2').css({'opacity': (intensity / 100)});
    $('.intensity-val span').html(intensity);

}

$(function(){
    $('.cube-switch .switch').click(function() {
        if ($('.cube-switch').hasClass('active')) {
            $('.cube-switch').removeClass('active');
        } else {
            $('.cube-switch').addClass('active');
        }
    });

    $('.door-switch .switch').click(function() {
        if ($('.door-switch').hasClass('active')) {
            $('.door-switch').removeClass('active');
        } else {
            $('.door-switch').addClass('active');
        }
    });




    $( "#bulb-intensity" ).slider({
        orientation: "horizontal",
        range: "min",
        min:0,
        max: 100,
        value: 127,
        slide: refreshSwatch,
        change: refreshSwatch
    });
    $( "#bulb-intensity" ).slider( "value", 0 );
});