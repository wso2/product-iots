$(function () {
    $("button#add-group-btn").click(function () {

        var name = $("input#name").val();
        var description = $("input#description").val();

        if (!name) {
            $('.wr-validation-summary strong').text("Group Name is a required field. It cannot be empty.");
            $('.wr-validation-summary').removeClass("hidden");
            return false;
        } else {

            var addGroupAPI = $("#group-api-ep").val();

            var group = {"name": name, "description": description};
            invokerUtil.post(
                addGroupAPI,
                group,
                function (data, txtStatus, jqxhr) {
                    var status = jqxhr.status;
                    if (status == 200) {
                        if (data != "false") {
                            $('.wr-validation-summary strong').text("Group created. You will be redirected to groups");
                            $('.wr-validation-summary').removeClass("hidden");
                            $('.wr-validation-summary strong').removeClass("label-danger");
                            $('.wr-validation-summary strong').addClass("label-success");
                            setTimeout(function () {
                                history.go(-1);
                            }, 1500);
                        } else {
                            $('.wr-validation-summary strong').text("Exception at backend.");
                            $('.wr-validation-summary strong').removeClass("label-danger");
                            $('.wr-validation-summary strong').addClass("label-warning");
                        }
                    } else if (status == 400) {
                        $('.wr-validation-summary strong').text("Exception at backend.");
                        $('.wr-validation-summary strong').removeClass("label-danger");
                        $('.wr-validation-summary strong').addClass("label-warning");
                    } else if (status == 403) {
                        $('.wr-validation-summary strong').text("Action not permitted.");
                    } else if (status == 409) {
                        $('.wr-validation-summary strong').text("Group exists.");
                        $('.wr-validation-summary strong').removeClass("label-default");
                        $('.wr-validation-summary strong').addClass("label-success");
                    }
                }, function () {
                    $('.wr-validation-summary strong').text("An unexpected error occurred.");
                    $('.wr-validation-summary').removeClass("hidden");
                    return false;
                }
            );

         }
    });
});



