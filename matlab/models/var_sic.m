function [yHat] = var_sic(y)
    mods = cell(5, 1);
    bic = zeros(5, 1);
    parfor j=1:size(mods, 1)
        mod = varm(5, j);
        res = estimate(mod, y, 'Display', 'off');
        mods{j} = res;
        bic(j) = summarize(res).BIC;
    end
    [~,idx] = sort(bic);
    yHat = forecast(mods{idx(1)}, 1, y);
end

