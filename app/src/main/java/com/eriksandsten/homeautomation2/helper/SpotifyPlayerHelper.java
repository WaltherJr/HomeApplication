package com.eriksandsten.homeautomation2.helper;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Kontext-menyn för en låt skapas i https://open.spotifycdn.com/cdn/build/mobile-web-player/vendor~mobile-web-player.a5ed9525.js (Lägg till custom entries)
 */
public final class SpotifyPlayerHelper {
    public record MenuItem(String actionId, String text) {}

    public static String injectContextMenuItems(String originalJavaScript, MenuItem... menuItemStr) {
        // String working1 = Arrays.stream(menuItemStr).map(menuItem -> "z.insertAdjacentHTML('beforebegin', z.outerHTML.replace('Visa spår', '" + menuItem.text + "'))").collect(Collectors.joining(","));
        String insertAdjacentHTMLSnippet = Arrays.stream(menuItemStr).map(menuItem -> "z.insertAdjacentHTML('beforebegin', z.outerHTML.replace('Visa spår', '%s').replace('<button', '<button data-action-id=\"%s\"'))"
                .formatted(menuItem.text, menuItem.actionId)).collect(Collectors.joining(","));

        // String works1 = "((z=document.createElement('button'),z.innerHTML='hejsan',e.prepend(z)), t=n).appendChild(e)";
        // String works2 = "((z=e.children[0], z.innerHTML=z.innerHTML.replace('Gilla','GILLA!')), t=n).appendChild(e)";
        // String works3 = "((z=e.querySelector('.MlWxT7rKXrHQxSRJJmLg > div:nth-child(3)') || {innerHTML: ''}), t=n).appendChild(e)";
        // String works4 = "((z=e.querySelector('.MlWxT7rKXrHQxSRJJmLg > div:nth-child(3)') || {innerHTML: ''}, z.innerHTML=z.innerHTML.replace('Visa', 'Göm')), t=n).appendChild(e)";
        String replacingSnippet = "((z=e.querySelector('.MlWxT7rKXrHQxSRJJmLg > div:nth-child(3)') || {outerHTML: '', insertAdjacentHTML: ()=>{}}, " + insertAdjacentHTMLSnippet + "), t=n).appendChild(e)";

        return originalJavaScript.replace("(t=n).appendChild(e)", replacingSnippet);
    }
}
