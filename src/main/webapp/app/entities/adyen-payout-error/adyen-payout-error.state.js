(function() {
    'use strict';

    angular
        .module('adyenMiraklConnectorApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('adyen-payout-error', {
            parent: 'entity',
            url: '/adyen-payout-error',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.adyenPayoutError.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-errors.html',
                    controller: 'AdyenPayoutErrorController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('adyenPayoutError');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('adyen-payout-error-detail', {
            parent: 'adyen-payout-error',
            url: '/adyen-payout-error/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'adyenMiraklConnectorApp.adyenPayoutError.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-error-detail.html',
                    controller: 'AdyenPayoutErrorDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('adyenPayoutError');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'AdyenPayoutError', function($stateParams, AdyenPayoutError) {
                    return AdyenPayoutError.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'adyen-payout-error',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('adyen-payout-error-detail.edit', {
            parent: 'adyen-payout-error-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-error-dialog.html',
                    controller: 'AdyenPayoutErrorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AdyenPayoutError', function(AdyenPayoutError) {
                            return AdyenPayoutError.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('adyen-payout-error.new', {
            parent: 'adyen-payout-error',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-error-dialog.html',
                    controller: 'AdyenPayoutErrorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                rawRequest: null,
                                rawResponse: null,
                                processing: null,
                                createdAt: null,
                                updatedAt: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('adyen-payout-error', null, { reload: 'adyen-payout-error' });
                }, function() {
                    $state.go('adyen-payout-error');
                });
            }]
        })
        .state('adyen-payout-error.edit', {
            parent: 'adyen-payout-error',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-error-dialog.html',
                    controller: 'AdyenPayoutErrorDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['AdyenPayoutError', function(AdyenPayoutError) {
                            return AdyenPayoutError.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('adyen-payout-error', null, { reload: 'adyen-payout-error' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('adyen-payout-error.delete', {
            parent: 'adyen-payout-error',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/adyen-payout-error/adyen-payout-error-delete-dialog.html',
                    controller: 'AdyenPayoutErrorDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['AdyenPayoutError', function(AdyenPayoutError) {
                            return AdyenPayoutError.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('adyen-payout-error', null, { reload: 'adyen-payout-error' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
