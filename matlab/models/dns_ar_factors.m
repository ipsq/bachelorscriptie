function [yHat] = dns_ar_factors(y, trainingMaturities, f)
    lambda0 = 0.0609;
    Xtrain = [ones(size(trainingMaturities')) (1-exp(-lambda0*trainingMaturities'))./(lambda0*trainingMaturities') ...
        ((1-exp(-lambda0*trainingMaturities'))./(lambda0*trainingMaturities')-exp(-lambda0*trainingMaturities'))];

    beta = zeros(size(y, 1), 3);
    yy = y(:, trainingMaturities / 12);
    parfor j = 1:size(y, 1)
        EstMdlOLS = fitlm(Xtrain, yy(j, :), 'Intercept', false);
        beta(j,:) = EstMdlOLS.Coefficients.Estimate';
    end
    
    mod1 = arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian');
    res1 = estimate(mod1,beta(:,1),'Display','off');
    mod2 = arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian');
    res2 = estimate(mod2,beta(:,2),'Display','off');
    mod3 = arima('Constant',NaN,'ARLags',1,'Distribution','Gaussian');
    res3 = estimate(mod3,beta(:,3),'Display','off');
    
    fbeta1 = forecast(res1, 1, beta(:,1));
    fbeta2 = forecast(res2, 1, beta(:,2));
    fbeta3 = forecast(res3, 1, beta(:,3));
    
    maturities = [12 24 36 60 120]';
    X = [ones(size(maturities)) (1-exp(-lambda0*maturities))./(lambda0*maturities) ...
        ((1-exp(-lambda0*maturities))./(lambda0*maturities)-exp(-lambda0*maturities))];
    
    yHat = X * [fbeta1 fbeta2 fbeta3]';
end

