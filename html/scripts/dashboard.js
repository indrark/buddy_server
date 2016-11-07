var requestHeaders = { "authorization":$.cookie('authorization') };
var current_uid = -1;

function getDate(timestamp) {
    var date = new Date(timestamp);

    var year = date.getFullYear();
    var month = "0" + (date.getMonth() + 1);
    var day = "0" + date.getDate();

    var hours = "0" + date.getHours();
    var minutes = "0" + date.getMinutes();
    var seconds = "0" + date.getSeconds();

    return month.substr(-2) + '/' + day.substr(-2) + '/' + year + ' ' + hours.substr(-2) + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
}

function showPlaceholder() {
    $("[id='placeholder']").css("display", "block");
}

function hidePlaceholder() {
    $("[id='placeholder']").css("display", "none");
}

function updatePostPageControl() {
    var current_page = $("[id='value-current-page']").text();
    $("[id='post-control-previous']").prop('disabled', parseInt(current_page) <= 1);
    $("[id='post-control-next']").prop('disabled', parseInt(current_page) <= 0);
    $("[id='post-control-goto']").prop('disabled', parseInt(current_page) <= 0);
}

function onServerStatusError() {
    hidePlaceholder();
    $("[id='status-user-count']").text("error");
    $("[id='status-post-count']").text("error");
}

function doUpdateServerStatus(data) {
    hidePlaceholder();
    if(data.response_code == 1) {
        $("[id='status-user-count']").text(data.user_count);
        $("[id='status-post-count']").text(data.post_count);
    } else {
        onServerStatusError();
    }
}

function tryUpdateServerStatus() {
    showPlaceholder();
    $.ajax({
        type: "POST",
        url: "/server/status",
        data: "{}",
        headers: requestHeaders,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(data){
            doUpdateServerStatus(data);
        },
        failure: function(errMsg) {
            onServerStatusError();
        }
    });
}

function getPostSearchSettings(page) {
    var uid = $("[id='input-post-uid']").val();
    var attention = $("[id='checkbox-attention']").is(":checked") ? 1 : 0;
    var flagged = $("[id='checkbox-flag']").is(":checked") ? 1 : -1;
    return "{\"page\": "+page+", \"category\": -1, \"attention\": "+attention+", \"target_uid\": "+uid+", \"flagged\": "+flagged+"}";
}

function onPostSearchError() {
    hidePlaceholder();
    $("[id='post-list']").text("error");
    $("[id='value-current-page']").text(0);
    updatePostPageControl();
}

function onPostSearchSuccess(data, page) {
    hidePlaceholder();
    $("[id='value-current-page']").text((page + 1));
    if(data.response_code == 1) {
        $("[id='post-list']").empty();
        if(data.posts.length <= 0) {
            $("[id='post-list']").text("No result");
        }
        for(var i = 0; i < data.posts.length; i++) {
            var post_content =
                "<div class=\"post-container\" id=\"post-"+data.posts[i].pid+"\">"
                + "[PID: " + data.posts[i].pid + "][UID: " + data.posts[i].uid + "]&nbsp"
                + "<span class=\"value-post-username\">" + data.posts[i].username + "</span>"
                + "&nbsp[" + getDate(data.posts[i].timestamp) + "]&nbsp"
                + "<span class=\"span-post-comments\">Comments:&nbsp" + data.posts[i].comments + "</span>"
                + "<span class=\"span-post-flags\">&nbspFlags:&nbsp" + data.posts[i].flags + "</span>"
                + "<br>"
                + "&nbsp<button class=\"button-post-delete\" onclick=\"tryDeletePost("+data.posts[i].pid+")\">Delete</button>"
                + "<span class=\"span-post-content\">\"" + data.posts[i].content + "\"</span>"
                + "</div>";
            $("[id='post-list']").append(post_content);
        }
        updatePostPageControl();
    } else {
        onPostSearchError();
    }
}

function trySearchPostPage(page) {
    showPlaceholder();
    $.ajax({
        type: "POST",
        url: "/post/list",
        data: getPostSearchSettings(page),
        headers: requestHeaders,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(data){
            onPostSearchSuccess(data, page);
        },
        failure: function(errMsg) {
            onPostSearchError();
        }
    });
}

function refreshCurrentPostPage() {
    var current_page = $("[id='value-current-page']").text();
    trySearchPostPage(parseInt(current_page) - 1);
}

function gotoPreviousPostPage() {
    var current_page = $("[id='value-current-page']").text();
    trySearchPostPage(parseInt(current_page) - 2);
}

function gotoNextPostPage() {
    var current_page = $("[id='value-current-page']").text();
    trySearchPostPage(parseInt(current_page));
}

function gotoTargetPostPage() {
    var target_page = $("[id='input-target-post-page']").val();
    trySearchPostPage(target_page - 1);
}

function onPostDeletingFinish(data) {
    if(data.response_code == 1) {
        refreshCurrentPostPage();
    } else {
        alert("Operation failed! Error code [" + data.response_code + "]");
    }
}

function tryDeletePost(pid) {
    var result = confirm("Are you sure you want to delete this post? (This operation cannot be undone.)");
    if(result == true) {
        showPlaceholder();
        $.ajax({
            type: "POST",
            url: "/post/delete",
            data: "{\"pid\": "+pid+"}",
            headers: requestHeaders,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function(data){
                onPostDeletingFinish(data);
            },
            failure: function(errMsg) {
                hidePlaceholder();
                alert("Operation failed! " + errMsg);
            }
        });
    }
}

function getUserSearchSettings() {
    var uid = $("[id='input-user-uid']").val();
    var email = $("[id='input-user-email']").val();
    var username = $("[id='input-user-username']").val();
    return "{\"uid\": "+uid+", \"email\": \""+email+"\", \"username\": \""+username+"\"}";
}

function onUserSearchSuccess(data) {
    hidePlaceholder();
    if(data.response_code == 1) {
        current_uid = data.uid;
        $("[id='user-data']").empty();
        var user_info =
            "<div class=\"user-container\">"
            + "UID:&nbsp" + data.uid
            + "<br>Email:&nbsp" + data.email
            + "<br>Username:&nbsp" + data.username
            + "<br>Posts:&nbsp" + data.posts
            + "</div>";
        $("[id='user-data']").append(user_info);
        var user_operation =
            "<div align=\"left\">"
            + "<h3>Operations</h3>"
            + "Reset Password:&nbsp<input type=\"text\" id=\"input-user-password\">"
            + "&nbsp<button onclick=\"tryResetUserPassword()\">Reset</button>"
            + "&nbsp<span id=\"value-password-reset-result\"></span>"
            + "</div>";
        $("[id='user-data']").append(user_operation);
    } else if (data.response_code == 6) {
        current_uid = -1;
        $("[id='user-data']").empty();
        $("[id='user-data']").text("Not found");
    } else {
        alert(data.response_code);
        onUserSearchError();
    }
}

function onUserSearchError() {
    hidePlaceholder();
    current_uid = -1;
    $("[id='user-data']").empty();
    $("[id='user-data']").text("Error");
}

function trySearchUser() {
    showPlaceholder();
    $.ajax({
        type: "POST",
        url: "/user/info",
        data: getUserSearchSettings(),
        headers: requestHeaders,
        contentType: "application/json; charset=utf-8",
        ataType: "json",
        success: function(data) {
            onUserSearchSuccess(data);
        },
        failure: function(errMsg) {
            onUserSearchError();
        }
    });
}

function getUserPasswordResetSettings() {
    var password = $("[id='input-user-password']").val();
    return "{\"uid\": "+current_uid+", \"password\": \""+password+"\"}";
}

function onUserPasswordResetSuccess(data) {
    hidePlaceholder();
    $("[id='value-password-reset-result']").css("color", "red");
    if(data.response_code == 1) {
        $("[id='value-password-reset-result']").css("color", "green");
        $("[id='value-password-reset-result']").text("Success!");
    } else if(data.response_code == 4) {
        $("[id='value-password-reset-result']").text("ERROR: Password not valid");
    } else if(data.response_code == 6) {
        $("[id='value-password-reset-result']").text("ERROR: User not found");
    } else {
        $("[id='value-password-reset-result']").text("Operation failed! Error code [" + data.response_code + "]");
    }
}

function tryResetUserPassword() {
    if(current_uid > 0) {
        var result = confirm("Are you sure you want to reset this user's password? (This operation cannot be undone.)");
        if(result != true) return;
        showPlaceholder();
        $.ajax({
            type: "POST",
            url: "/password/reset",
            data: getUserPasswordResetSettings(),
            headers: requestHeaders,
            contentType: "application/json; charset=utf-8",
            ataType: "json",
            success: function(data) {
                onUserPasswordResetSuccess(data);
            },
            failure: function(errMsg) {
                hidePlaceholder();
                alert("Operation failed! " + errMsg);
            }
        });
    }
}