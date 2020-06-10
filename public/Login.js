var loginButton = $('#login');
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

            error: function (data) 
            {
                alert(data.message);     
            },
            success: function (data) 
            {
                    if(!data.error)
                    {   
                        window.location ="/";   
                    }
                    else
                    {
                        alert(data.message);
                    } 
            }
        });

});

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

            error: function (data) 
            {
                alert(data.message); 
            },
            success: function (data) 
            { 
                alert(data.message)
            }

        });
});