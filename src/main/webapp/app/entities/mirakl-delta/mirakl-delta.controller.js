(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .controller('MiraklDeltaController', MiraklDeltaController);

    MiraklDeltaController.$inject = ['MiraklDelta'];

    function MiraklDeltaController(MiraklDelta) {

        var vm = this;

        vm.miraklDeltas = [];

        loadAll();

        function loadAll() {
            MiraklDelta.query(function(result) {
                vm.miraklDeltas = result;
                vm.searchQuery = null;
            });
        }
    }
})();
