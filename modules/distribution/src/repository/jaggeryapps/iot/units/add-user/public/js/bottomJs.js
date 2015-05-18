$( document ).ready(function() {
    $("select.select2").select2({
        placeholder : "Select..."
    });

    $("select.select2[multiple=multiple]").select2({
        placeholder : "Select...",
        tags : true
    });
});

/**
 * Checks if an email address has the valid format or not.
 *
 * @param email Email address
 * @returns {boolean} true if email has the valid format, otherwise false.
 */
var emailIsValid = function(email) {
    var atPosition = email.indexOf("@");
    var dotPosition = email.lastIndexOf(".");
    return !(atPosition < 1 || ( dotPosition - atPosition < 2 ));
};

/**
 * Following click function would execute
 * when a user clicks on "Add User" button
 * on Add User page in WSO2 MDM Console.
 */
$("button#add-user-btn").click(function() {
    var username = $("input#username").val();
    var firstname = $("input#firstname").val();
    var lastname = $("input#lastname").val();
    var emailAddress = $("input#email").val();
    var userRoles = $("select#roles").val();

    if (!username) {
        $(".wr-validation-summary p").text("Username is a required field. It cannot be empty.");
        $(".wr-validation-summary").removeClass("hidden");
    } else if (!firstname) {
        $(".wr-validation-summary p").text("Firstname is a required field. It cannot be empty.");
        $(".wr-validation-summary").removeClass("hidden");
    } else if (!lastname) {
        $(".wr-validation-summary p").text("Lastname is a required field. It cannot be empty.");
        $(".wr-validation-summary").removeClass("hidden");
    } else if (!emailAddress) {
        $(".wr-validation-summary p").text("Email is a required field. It cannot be empty.");
        $(".wr-validation-summary").removeClass("hidden");
    } else if (!emailIsValid(emailAddress)) {
        $(".wr-validation-summary p").text("Email is not valid. Please enter a correct email address.");
        $(".wr-validation-summary").removeClass("hidden");
    } else {
        var addUserFormData = {};
        addUserFormData.username = username;
        addUserFormData.firstname = firstname;
        addUserFormData.lastname = lastname;
        addUserFormData.emailAddress = emailAddress;
        addUserFormData.userRoles = userRoles;

        var addUserAPI = "/mdm/api/users/add";

        $.ajax({
            type : "POST",
            url : addUserAPI,
            contentType : "application/json",
            data : JSON.stringify(addUserFormData),
            success : function(data) {
                if (data == 201) {
                    $(".wr-validation-summary p").text("User (" + username + ") was added. " +
                        "An invitation mail will also be sent to this user to initiate a device enrollment.");
                    // Clearing user input fields.
                    $("input#username").val("");
                    $("input#firstname").val("");
                    $("input#lastname").val("");
                    $("input#email").val("");
                    $("select#roles").select2("val", "");
                } else if (data == 400) {
                    $(".wr-validation-summary p").text("Exception occurred at backend.");
                } else if (data == 403) {
                    $(".wr-validation-summary p").text("Action was not permitted.");
                } else if (data == 409) {
                    $(".wr-validation-summary p").text("Sorry, User already exists.");
                }
                $(".wr-validation-summary").removeClass("hidden");
            },
            error : function() {
                $(".wr-validation-summary p").text("An unexpected error occurred.");
                $(".wr-validation-summary").removeClass("hidden");
            }
        });
    }
});