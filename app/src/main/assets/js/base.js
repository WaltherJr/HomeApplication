
function unwrapRestCall(str) {
    var response = JSON.parse(str);

    if (response.responseType === 'ERROR') {
        showPopupBox(response.wrappedValue.title, response.wrappedValue.message);
    }

    return response;
}

function showPopupBox(title, message) {
    // TODO: use javaInterface
}
