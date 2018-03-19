(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('AdyenPayoutErrorController', AdyenPayoutErrorController);

    AdyenPayoutErrorController.$inject = ['DataUtils', 'AdyenPayoutError'];

    function AdyenPayoutErrorController(DataUtils, AdyenPayoutError) {

        var vm = this;

        vm.adyenPayoutErrors = [];
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;

        loadAll();

        function loadAll() {
            AdyenPayoutError.query(function(result) {
                vm.adyenPayoutErrors = result;
                vm.searchQuery = null;
            });
        }
    }
})();
