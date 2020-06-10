function [yHat] = var_1(y)
    mod = varm(5, 1);
    res = estimate(mod, y, 'Display', 'off');
    yHat = forecast(res, 1, y);
end

