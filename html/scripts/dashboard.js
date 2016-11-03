var requestHeaders = { "authorization":$.cookie('authorization') };

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
        url: "/admin/status",
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
    var uid = $("[id='input-uid']").val();
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
                + data.posts[i].username + "[" + data.posts[i].uid + "][" + getDate(data.posts[i].timestamp) + "]&nbspFlags:&nbsp" + data.posts[i].flags
                + "<br>"
                + "&nbsp<button class=\"button-post-delete\" onclick=\"tryDeletePost("+data.posts[i].pid+")\">Delete</button>"
                + "&nbsp\"" + data.posts[i].content + "\"&nbsp"
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
                alert("Operation failed! " + errMsg);
            }
        });
    }
}
