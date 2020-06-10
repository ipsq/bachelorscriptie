function [yHat] = var_sic_factors(y, f)
    mods = cell(5, 1);
    bic = zeros(5, 1);
    parfor j=1:size(mods, 1)
        mod = varm(5, j);
        res = estimate(mod, y, 'X', f(1:end-1, :), 'Display', 'off');
        mods{j} = res;
        bic(j) = summarize(res).BIC;
    end
    [~,idx] = sort(bic);
    yHat = forecast(mods{idx(1)}, 1, y, 'X', f(end, :));
end

