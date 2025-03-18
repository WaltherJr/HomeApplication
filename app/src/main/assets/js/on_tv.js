
$(document).ready(function() {
/*    var iframe = $('<iframe onload="javascript:(function() {' + javaInterface.getYoutubeFrameJS() + '})()"></iframe>');
    iframe.css({'position': 'fixed', 'z-index': '99999', 'width': 'calc(100% - 20px)', 'height': '50%', 'border': '3px solid lightgray', 'border-radius': '4px', 'box-sizing': 'border-box', 'left': '10px', 'top': '50px', 'filter': 'drop-shadow(5px 5px 15px)'});
    iframe.attr({'id': 'youtube-search-results', 'src': 'https://www.youtube.com/results?output=embed&search_query=backstreet+boys+i+want+it+that+way'});
    iframe.on('click', function() {
        alert('CLicked irame!')
    });
    $('body').append(iframe);
*/

    setTimeout(function() {
        $('epg-date').append('<label id="only-show-subscribed-tv-channels-label"><input id="only-show-subscribed-tv-channels-checkbox" type="checkbox"> Visa bara abonnerade kanaler</label>')
                    .append('<button class="show-epg-btn">Visa EPG</button').append('<button class="show-hide-btn">Göm</button>');
        var container = $('<div id="extra-control-panel"></div>');
        $(container).prepend($('epg-date'));
        $(container).prepend($('epg-filter'));
        $('.agenda-data').prepend(container);
        $('body').addClass('agenda-data-modified');

        $('div[data-channel-id]').each(function(button) {
            var channelId = $(this).attr('data-channel-id');
            var channelMapping = javaInterface.getTVChannelMapping(channelId);

            $(this).attr('data-channel-mapping', channelMapping);

            if (javaInterface.isSubscribedTVChannel(channelId)) {
                $(this).addClass('is-subscribed-tv-channel');
                $(this).find('.epg-header').append('<button class="play-pause-btn">Play</button>');
            }
        });

        $('body').on('click', '.play-pause-btn', function() {
            console.log('CHANGING STATE!');
            $(this).toggleClass('is-playing');
            javaInterface.setActiveTVChannelPlayState($(this).hasClass('is-playing') ? 'true' : 'false');
        }).on('click', '.show-hide-btn', function() {
            var extraControlPanel = $('#extra-control-panel');

            if (extraControlPanel.toggleClass('hidden').hasClass('hidden')) {
                $(this).text('Visa')
                extraControlPanel.css('margin-top', '-' + (Math.round(extraControlPanel.outerHeight() - $(this).outerHeight() - 8)) + 'px');
            } else {
                $(this).text('Göm')
                extraControlPanel.css('margin-top', 'initial');
            }
        }).on('click', '.show-epg-btn', function() {
            javaInterface.viewTVChannelEPG();
        });

        $('epg-image').on('click', function() {
            var subscribedChannel = $(this).closest('div[data-channel-id]').attr('data-channel-mapping');
            var activeChannel = $(document).data('activeChannel') || {channelId: null, isPlaying: false};

            console.log('ACTIVE CHANNEL!: ' + activeChannel.channelId);

            if (subscribedChannel && activeChannel.channelId !== subscribedChannel) {
                var channelContainer = $(this).closest('.epg-channel-container')
                channelContainer.addClass('channel-loading');
                alert('setting active channel...' + channelContainer.length);
                javaInterface.setActiveTVChannel(subscribedChannel);
                channelContainer.removeClass('channel-loading');
            }
        });

        $('body').on('change', '#only-show-subscribed-tv-channels-checkbox', function() {
            $('div[data-channel-id]:not(.is-subscribed-tv-channel)').css('display', $(this).is(':checked') ? 'none' : 'initial')
        });
    }, 2000);

    setInterval(function() {
        var activeChannel = $('div[data-channel-id].active-channel');
        activeChannel.removeClass('active-channel').find('.play-pause-btn').removeClass('is-playing');
        var activeChannelJSON = javaInterface.getActiveTVChannel();

        if (activeChannelJSON) {
            var updatedActiveChannel = JSON.parse(activeChannelJSON);
            console.log('SETTING ACTIVE CHANNEL! ' + updatedActiveChannel.channelId);
            $(document).data('activeChannel', updatedActiveChannel);
            $('div[data-channel-id]').filter(`[data-channel-mapping="${updatedActiveChannel.channelId}"]`)
                .addClass('active-channel').find('button.play-pause-btn').addClass('is-playing');
        }
    }, 2000);
});
