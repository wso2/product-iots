var emailIsValid = function (email) {
    var atPosition = email.indexOf("@");
    var dotPosition = email.lastIndexOf(".");
    return !(atPosition < 1 || ( dotPosition - atPosition < 2 ));
};

var validatePassword = function (psswd, conPass) {
    var error = "";
    var illegalChars = /[\W_]/; // allow only letters and numbers

    if ((psswd.length < 5) || (psswd.length > 15)) {
        error = "The password is of wrong length. Should be between 5 and 15 characters. \n";
        $('.wr-validation-summary strong').text(error);
        $('.wr-validation-summary').removeClass("hidden");
        return false;

    } else if (illegalChars.test(psswd)) {
        error = "The password contains illegal characters.\n";
        $('.wr-validation-summary strong').text(error);
        $('.wr-validation-summary').removeClass("hidden");
        return false;

    } else if ((psswd.search(/[a-zA-Z]+/) == -1) || (psswd.search(/[0-9]+/) == -1)) {
        error = "The password must contain at least one numeral and one character.\n";
        $('.wr-validation-summary strong').text(error);
        $('.wr-validation-summary').removeClass("hidden");
        return false;

    } else if (psswd != conPass) {
        error = "The password and confirm-password should match.\n";
        $('.wr-validation-summary strong').text(error);
        $('.wr-validation-summary').removeClass("hidden");
        // return false;

    } else {
        return true;
    }

};

$(function () {
    $("button#add-user-btn").click(function () {
        //e.preventDefault();
        var username = $("input#user_name").val();
        var firstname = $("input#first_name").val();
        var lastname = $("input#last_name").val();
        var emailAddress = $("input#email").val();
        var password = $("input#password").val();
        var passwordConfirm = $("input#password_confirmation").val();

        // var userRoles = $("select#roles").val();

        if (!firstname) {
            $('.wr-validation-summary strong').text("Firstname is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
            return false;
        } else if (!lastname) {
            $('.wr-validation-summary strong').text("Lastname is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!username) {
            $('span.wr-validation-summary strong').text("Username is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!emailAddress) {
            $('.wr-validation-summary strong').text("Email is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!emailIsValid(emailAddress)) {
            $('.wr-validation-summary strong').text("Email is not valid. Please enter a correct email address.");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!password) {
            $('.wr-validation-summary strong').text("Please enter a user login password.");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!passwordConfirm) {
            $('.wr-validation-summary strong').text("Please re-type password");
            $('.wr-validation-summary').removeClass("hidden");
             return false;
        } else if (!validatePassword(password, passwordConfirm)) {
             return false;
        } else if (!$('#t_and_c').is(':checked')) {
            $('.wr-validation-summary strong').text("Please accept our terms and conditions");
            $('.wr-validation-summary').removeClass("hidden");
        } else {

            var addUserFormData = {};
            addUserFormData.username = username;
            addUserFormData.firstname = firstname;
            addUserFormData.lastname = lastname;
            addUserFormData.emailAddress = emailAddress;
            addUserFormData.password = password;
            addUserFormData.userRoles = null;

            var addUserAPI = "/iotserver/api/users/register";

            $.ajax({
                type: 'POST',
                url: addUserAPI,
                contentType: 'application/json',
                data: JSON.stringify(addUserFormData),
                success: function (data) {
                    if (data == 200) {
                        $('.wr-validation-summary strong').text("Successfully Submitted.");
                        $('.wr-validation-summary strong').removeClass("label-danger");
                        $('.wr-validation-summary strong').addClass("label-success");
                    } else if (data == 201) {
                        $('.wr-validation-summary strong').text("User created.");
                        $('.wr-validation-summary strong').removeClass("label-danger");
                        $('.wr-validation-summary strong').addClass("label-success");
                        window.location = "/iotserver/login";
                    } else if (data == 400) {
                        $('.wr-validation-summary strong').text("Exception at backend.");
                        $('.wr-validation-summary strong').removeClass("label-danger");
                        $('.wr-validation-summary strong').addClass("label-warning");
                    } else if (data == 403) {
                        $('.wr-validation-summary strong').text("Action not permitted.");
                    } else if (data == 409) {
                        $('.wr-validation-summary strong').text("User exists.");
                        $('.wr-validation-summary strong').removeClass("label-default");
                        $('.wr-validation-summary strong').addClass("label-success");
                    }
                    $('.wr-validation-summary').removeClass("hidden");
                    $('#password').val('');
                    $('#password_confirmation').val('');
                    // return true;
                },
                error: function () {
                    $('.wr-validation-summary strong').text("An unexpected error occurred.");
                    $('.wr-validation-summary').removeClass("hidden");
                     return false;
                }
            });
        }
    });
});



