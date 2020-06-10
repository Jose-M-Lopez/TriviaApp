checkLogin().then(function(isLoggedIn)
{
    if(isLoggedIn === true)
    {
        $('#login').hide();
        $('#play').show();
        $('#logout').show();
        $('#matchHistory').show();
    }
  
})

