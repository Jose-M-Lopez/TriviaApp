checkLogin().then(function(isLoggedIn)
{
    if(isLoggedIn === false)
    {
        window.location="/";
    }

});

function diffSelect(val) {
    var diffRequest = $.ajax(
        {
            url: "/api/startGame",
            method: "POST",
            data: {
                'difficulty': val
            },
            dataType: "json",

            error: function (data) {
                alert(data.message);
            },

            success: function (data) 
            {
                window.location = "Round.html";
            }
        }
    )
};

setTimeout(function (){
    if(localStorage.getItem("New")===null)
    {
        alert("Earn extra points by answering in under 10 seconds!");
        localStorage.setItem("New", false);
    }
},100);
