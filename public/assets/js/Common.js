//Check whether or not user is logged in.
function checkLogin() {
    return $.ajax(
        {
            url: "/api/checkLogin",
            method: "GET",
            dataType: "json",
        });
}

//Allow user to log out, redirects to home.
$(function () {
    $('#logout').click(function submitLogin(event) {
        event.preventDefault();

        var logoutRequest = $.ajax(
            {
                url: "/api/Logout",
                method: "POST",
                dataType: "json",

                error: function (data) {
                    alert(data.message);
                },

                success: function (data) {
                    window.location = "/";
                }
            });
    });
});
