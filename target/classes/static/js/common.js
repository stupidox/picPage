function encode(str) {
    if (str !== null && str !== "" && typeof(str) === 'string') {
        return str.replaceAll('\\', '%2f').replaceAll('[', '%5B').replaceAll(']', '%5D').replaceAll('+', '%2B')
            .replaceAll('&', '%26').replaceAll('▲', '%e2%96%b2').replaceAll('▼', '%e2%96%bc')
    }
    return str
}

function decode(str) {
    if (str !== null && str !== "" && typeof(str) === 'string') {
        return str.replaceAll('%2f', '\\').replaceAll('%5B', '[').replaceAll('%5D', ']').replaceAll('%2B', '+')
            .replaceAll('%26', '&').replaceAll('%e2%96%b2', '▲').replaceAll('%e2%96%bc', '▼')
    }
    return str
}

function getParentName(str) {
    if (str == null || str === '') {
        return '';
    }
    str = str.replaceAll('/', '\\');
    const i = str.lastIndexOf('\\');
    if (i > 0) {
        return str.substring(i+1);
    }
    return str;
}

function getPParentPath(str) {
    if (str == null || str === '') {
        return '';
    }
    str = str.replaceAll('/', '\\');
    const i = str.lastIndexOf('\\');
    if (i > 0) {
        return str.substring(0, i);
    }
    return str;
}