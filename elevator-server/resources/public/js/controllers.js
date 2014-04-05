var renderApp = angular.module('renderApp', ['stateService']);

renderApp.controller('RenderController', ['$scope', 'GameState', function ($scope, GameState) {
    //$scope.states = [{"client": {"name": "tiimi 1"}}];
    $scope.states = GameState.query();
}]);