var loginButton = $('#login');

//Sending form information to back-end and requesting to log in.
loginButton.click(function submitLogin(event) {
    event.preventDefault();

    var loginRequest = $.ajax(
        {
            url: "/api/Login",
            method: "POST",
            data: {
                'Username': $('#inputUsername').val(),
                'Password': $('#inputPassword').val()
            },
            dataType: "json",

            error: function (data) {
                alert(data.message);
            },
            success: function (data) {
                if (!data.error) {
                //Redirect to homepage on successful login.
                    window.location = "/";
                }
                else {
                    alert(data.message);
                }
            }
        });
});

//Sending form information to back-end and requesting a new account.
var createButton = $('#register');
createButton.click(function submitLogin(event) {
    event.preventDefault();

    var registerRequest = $.ajax(
        {
            url: "/api/createAccount",
            method: "POST",
            data: {
                'Username': $('#inputUsername').val(),
                'Password': $('#inputPassword').val()
            },
            dataType: "json",

            error: function (data) {
                alert(data.message);
            },
            success: function (data) {
                alert(data.message)
            }

        });
});