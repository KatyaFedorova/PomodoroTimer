(function () {
  const players = {};
  const pending = [];
  let apiRequested = false;

  function loadApiIfNeeded() {
    if (apiRequested) return;
    apiRequested = true;
    const tag = document.createElement("script");
    tag.src = "https://www.youtube.com/iframe_api";
    document.body.appendChild(tag);
  }

  function createPlayer(containerId, videoId, onWatched30Seconds, onEnded) {
    if (!onEnded) onEnded = () => {};
    if (!onWatched30Seconds) onWatched30Seconds = () => {};

    if (players[containerId]) {
      try { players[containerId].destroy(); } catch(_) {}
      delete players[containerId];
    }

    function _create() {
      let watchedTime = 0;
      let timer = null;
      let hasNotified = false;

      const player = new YT.Player(containerId, {
        height: '100%',
        width: '100%',
        videoId,
        playerVars: { autoplay: 1, controls: 1, modestbranding: 1, rel: 0, playsinline: 1 },
        events: {
          onReady: () => console.log("[YouTubeBridge] onReady", containerId),
          onStateChange: (event) => {
            if (event.data === YT.PlayerState.PLAYING) {
              if (timer) clearInterval(timer);
              timer = setInterval(() => {
                if (player.getPlayerState() === YT.PlayerState.PLAYING) {
                  watchedTime++;
                  if (watchedTime >= 30 && !hasNotified) {
                    hasNotified = true;
                    onWatched30Seconds();
                  }
                }
              }, 1000);
            } else {
              if (timer) {
                clearInterval(timer);
                timer = null;
              }
            }

            if (event.data === YT.PlayerState.ENDED) {
              if (timer) clearInterval(timer);
              onEnded();
            }
          },
          onError: (err) => console.error("[YouTubeBridge] Player error", containerId, err)
        }
      });

      players[containerId] = player;
      console.log("[YouTubeBridge] Player created", containerId);
    }

    if (window.YT && window.YT.Player) {
      _create();
    } else {
      pending.push({ containerId, videoId, onWatched30Seconds, onEnded });
      loadApiIfNeeded();
      if (!window._ytOnReadyInstalled) {
        window._ytOnReadyInstalled = true;
        window.onYouTubeIframeAPIReady = function () {
          while (pending.length) {
            const item = pending.shift();
            createPlayer(item.containerId, item.videoId, item.onWatched30Seconds, item.onEnded);
          }
        };
      }
    }
  }

  function destroyPlayer(containerId) {
    const p = players[containerId];
    if (p) {
      try { p.destroy(); } catch(_) {}
      delete players[containerId];
      console.log("[YouTubeBridge] Player destroyed", containerId);
    }
  }

  window.myYouTubeBridge = { createPlayer, destroyPlayer };
})();
