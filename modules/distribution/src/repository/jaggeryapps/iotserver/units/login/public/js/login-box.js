$( document ).ready(function() {
    var currentHash = window.location.hash;
    if(currentHash=="#auth-failed") {
        $('.wr-validation-summary p').text("Sorry!, Please make sure to enter correct username and password");
        $('.wr-validation-summary').removeClass("hidden");
    }else if(currentHash=="#error"){
        $('.wr-validation-summary p').text("Sorry!, Error occurred");
        $('.wr-validation-summary').removeClass("hidden");
    }
    $('.btn-download-agent').click(function(){
        $('.form-login-box').submit();
    });
});
