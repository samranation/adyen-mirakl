(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('ShareholderMappingController', ShareholderMappingController);

    ShareholderMappingController.$inject = ['ShareholderMapping'];

    function ShareholderMappingController(ShareholderMapping) {

        var vm = this;

        vm.shareholderMappings = [];

        loadAll();

        function loadAll() {
            ShareholderMapping.query(function(result) {
                vm.shareholderMappings = result;
                vm.searchQuery = null;
            });
        }
    }
})();
