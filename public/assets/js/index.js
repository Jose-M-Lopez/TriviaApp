//Altering home display based on if a user is logged in or not.
checkLogin().then(function (isLoggedIn) {
    if (isLoggedIn === true) {
        $('#login').hide();
        $('#play').show();
        $('#logout').show();
        $('#matchHistory').show();
    }
})

