function [yHat] = var_1_factors(y, f)
    mod = varm(5, 1);
    res = estimate(mod, y, 'X', f(1:end-1, :), 'Display', 'off');
    yHat = forecast(res, 1, y, 'X', f(end, :));
end

