Backgrounds, UIs, icons etc. can be downloaded from https://www.freepik.com

App icons: https://www.123rf.com/clipart-vector/luxury_rustic_home_interior.html

HDMI CEC: https://www.cec-o-matic.com/

SVG icons generated at https://www.vondy.com/free-ai-icon-generator--r8yJWaGJ

```
- What type of icon would you like to generate?
  App icon

- What style should the icon have?
  Minimalist

- What color scheme do you prefer?
  White and gray

- What motif?
  A round ceiling light | A table lamp | A large smart-TV | A small smart-TV | A Fractal Design Ridge PC chassi | A power outlet | Pleated blinds | A motion sensor
```

Använd Chaquopy för att använda Python i Android (Python behövs för pylips!)

Background generation at: https://www.recraft.ai/project/a7ef78c6-8e99-4887-abf4-10f6a4544491
"a laundry room"
"a bedroom"
"a living room"
"a hallway in an apartment"
TODO: remove `android:usesCleartextTraffic="true"`

## Custom Spotify meny
Sök efter denna funktion i JS-scriptet https://open.spotifycdn.com/cdn/build/mobile-web-player/vendor~mobile-web-player.a5ed9525.js:

`(z = document.createElement('button'), z.innerHTML = 'hejsan', document.getElementsByTagName('body').item(0)).prepend(z)` - funkar
Ersätt `(t = n).appendChild(e)` med `(z = document.createElement('button'), z.innerHTML = 'hejsan', e.appendChild(z), t = n).appendChild(e)`
```
function su(e, t, n) {
    var r = e.tag;
    if (5 === r || 6 === r)
        e = e.stateNode,
        t ? 8 === n.nodeType ? n.parentNode.insertBefore(e, t) : n.insertBefore(e, t) : (8 === n.nodeType ? (t = n.parentNode).insertBefore(e, n) : (t = n).appendChild(e),
            null != (n = n._reactRootContainer) || null !== t.onclick || (t.onclick = Zr));
    else if (4 !== r && null !== (e = e.child))
        for (su(e, t, n),
            e = e.sibling; null !== e;)
            su(e, t, n),
            e = e.sibling
}
```

```

<script>$(document).ready(setTimeout(function(){FeedbackDialog('Ditt valda pass fredag 14 mars 10:00-13:00 är bokat.', 'INFORMATION', 'Stäng'); }, 500));</script>

```
Use mutation observer to catch laundry booking removal data (can't use regular polling). HTML structure to watch for mutations:
```
<div id="ErrorMessageDiv" role="dialog" class="dialogOuterContainer" style="visibility: visible; display: block;">
    <div class="dialogInnerContainer">
        <label class="dialogHeaderText">Avboka?</label>
        <div class="dialogInformationText" id="PassData">Vill du avboka din bokning <b>16:00-19:00</b> på lördag 15 mars?</div>
    </div>
    <div style="position:relative;width:100%;background-color:#363d43;">
        <button id="NoButton" class="dialogCancelButton" aria-label="Avbryt" onclick="EnableGUI(); document.getElementById('ErrorMessageDiv').style.display = 'none';">Avbryt</button>
        <button id="YesButton" class="dialogConfirmButton" aria-label="Avboka" onclick="Unbook()">Avboka</button>
    </div>
    <div class="dialogBlockArea"></div>
</div>
```
```
<div role="dialog" tabindex="0" id="FeedbackDialogDiv" class="dialogOuterContainer" style="visibility: visible; display: block;">
    <div class="dialogInnerContainer">
        <label class="dialogHeaderText" id="FeedbackDialogHeadline">INFORMATION</label><br>
        <div class="dialogInformationText" id="FeedbackDialogInformation">Ditt pass har blivit avbokat.</div>
    </div>
    <div style="position:relative;width:100%; background-color:#363d43;">
        <button id="FeedbackDialogCloseButton" class="dialogCancelButton" style="width:100%" onclick="document.getElementById('FeedbackDialogDiv').style.display = 'none';" aria-label="Stäng">Stäng</button>
    </div>
</div>
```
## Scratchpad
```
chooseAccountActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> googleAccountCredentials.setSelectedAccountName(result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME)));

var accounts = AccountManager.get(getContext()).getAccountsByType(AccountManager.KEY_ACCOUNT_TYPE);
var chooseAccountIntent = googleAccountCredentials.newChooseAccountIntent();
chooseAccountActivityLauncher.launch(chooseAccountIntent);
```
