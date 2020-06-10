function [yHat] = ar_sic(y)
    yHat = zeros(1, size(y, 2));
    parfor i=1:size(y, 2)
        mods = cell(5, 1);
        bic = zeros(5, 1);
        for j=1:size(mods, 1)
            mod = arima('Constant', NaN, 'ARLags', 1:j, 'Distribution', 'Gaussian');
            res = estimate(mod, y(:, i), 'Display', 'off');
            mods{j} = res;
            bic(j) = summarize(res).BIC;
        end
        [~,idx] = sort(bic);
        yHat(1, i) = forecast(mods{idx(1)}, 1, y(:, i));
    end
end
