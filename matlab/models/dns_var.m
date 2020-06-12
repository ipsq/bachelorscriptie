function [yHat] = dns_var(y, trainingMaturities)
    lambda0 = 0.0609;
    Xtrain = [ones(size(trainingMaturities')) (1-exp(-lambda0*trainingMaturities'))./(lambda0*trainingMaturities') ...
        ((1-exp(-lambda0*trainingMaturities'))./(lambda0*trainingMaturities')-exp(-lambda0*trainingMaturities'))];

    beta = zeros(size(y, 1), 3);
    yy = y(:, trainingMaturities / 12);
    parfor j = 1:size(y, 1)
        EstMdlOLS = fitlm(Xtrain, yy(j, :), 'Intercept', false);
        beta(j,:) = EstMdlOLS.Coefficients.Estimate';
    end
    
    mod = varm(3,1);
    res = estimate(mod,beta,'Display','off');
    fbeta = forecast(res, 1, beta);
    
    maturities = [12 24 36 60 120]';
    X = [ones(size(maturities)) (1-exp(-lambda0*maturities))./(lambda0*maturities) ...
        ((1-exp(-lambda0*maturities))./(lambda0*maturities)-exp(-lambda0*maturities))];
    
    yHat = X * fbeta';
end

