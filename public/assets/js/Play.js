//Redirecting to homepage if not logged in.
checkLogin().then(function(isLoggedIn)
{
    if(isLoggedIn === false)
    {
        window.location="/";
    }

});

//Helper function to start the game once a difficulty is selected.
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

//Alert to inform first time users of the timer's functionality. 
setTimeout(function (){
    if(localStorage.getItem("New")===null)
    {
        alert("Earn extra points by answering in under 10 seconds!");
        localStorage.setItem("New", false);
    }
},100);
