function checkLogin()
{
    return $.ajax(
        {
            url: "/api/checkLogin",
            method: "GET",
            dataType: "json",
    
        }).then(function(data)
        {
            return data;
        });
}

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

                    success: function (data) 
                    {
                        window.location = "/";
                    }

                });
        
    });
});
