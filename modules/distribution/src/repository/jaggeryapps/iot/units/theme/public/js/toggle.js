$(document).ready(function(){
    $(".sign-toggle-btn").click(function(){
        var type = $(this).attr('data-type'),
            container = 'div.sign-panel[data-type="' + type + '"]';
        $(container).slideToggle("fast");
        $(container).siblings('div.sign-panel').slideUp("fast");
    });

    $(".cancel-btn").click(function(){
        $(".sign-panel").slideUp();
    });
});