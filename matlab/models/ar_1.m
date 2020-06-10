function [yHat] = ar_1(y)
    yHat = zeros(1, size(y, 2));
    for i=1:size(y, 2)
        mod = arima('Constant', NaN, 'ARLags', 1, 'Distribution', 'Gaussian');
        res = estimate(mod, y(:, i), 'Display', 'off');
        [yHat(1, i), ~] = forecast(res, 1, y(:, i));
    end
end
