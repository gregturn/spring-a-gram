define(function (require) {

    return {
        wrapSelfHref: wrapSelfHref
    };

    // Taken an href and serve it up via obj._links.self.href
    function wrapSelfHref(href) {
        return {
            _links: {
                self: {
                    href: href
                }
            }
        }
    }


});