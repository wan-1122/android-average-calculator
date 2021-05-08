package wan.app.accountmanager.ui.calculator;

import android.arch.lifecycle.ViewModelProvider;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import wan.app.accountmanager.R;

public class CalculatorFragment extends Fragment {
    private DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

    public class Baek1TextWatcher implements TextWatcher {
        private EditText targetEditText;
        private EditText otherEditText;
        private TextView textView1;

        private String prevStr;

        private ICalculator mCalculator;

        Baek1TextWatcher(EditText targetEditText, EditText otherEditText, TextView reulst, ICalculator calculator) {
            this.targetEditText = targetEditText;
            this.otherEditText = otherEditText;
            this.textView1 = reulst;

            this.mCalculator = calculator;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            this.prevStr = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                this.textView1.setText("");
                return;
            }

            String num = s.toString().replaceAll("[^0-9.]","");
            if (TextUtils.isEmpty(num)) {
                num = "0";
            }
            Double d = Double.parseDouble(num);
            String newStr = decimalFormat.format(d);
            if (!TextUtils.isEmpty(prevStr) && prevStr.equalsIgnoreCase(newStr)) {
                this.textView1.setText("");
                return;
            }
            targetEditText.removeTextChangedListener(this);
            targetEditText.setText(newStr);
            Selection.setSelection(targetEditText.getText(), newStr.length());
            targetEditText.addTextChangedListener(this);

            String input1 = null;
            String input2 = null;
            if ("input1".equalsIgnoreCase(targetEditText.getTag().toString())) {
                input1 = targetEditText.getText().toString();
                input2 = otherEditText.getText().toString();
            } else if ("input2".equalsIgnoreCase(targetEditText.getTag().toString())) {
                input1 = otherEditText.getText().toString();
                input2 = targetEditText.getText().toString();
            } else {
                return;
            }
            try {
                input1 = input1.replaceAll("[^0-9.]","");
                input2 = input2.replaceAll("[^0-9.]","");

                double d1 = Double.valueOf(TextUtils.isEmpty(input1) ? "0" : input1);
                double d2 = Double.valueOf(TextUtils.isEmpty(input2) ? "0" : input2);

                CalculatorVO vo = this.mCalculator.calculator(d1, d2);
                this.textView1.setText(vo.getText() == null ? "" : vo.getText());
                this.textView1.setTextColor(vo.getTextColor());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface ICalculator {
        CalculatorVO calculator(double input1, double input2);
    }
    public class CalculatorVO {
        private String text;
        private int textColor;
        private String messageFormat;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public String getMessageFormat() {
            return messageFormat;
        }

        public void setMessageFormat(String messageFormat) {
            this.messageFormat = messageFormat;
        }
    }

    public static String[] RESULT_KEYS = new String[] {
            AverageTextWatcher.CURRENT_TOTAL_PRICE, AverageTextWatcher.ADD_TOTAL_PRICE, AverageTextWatcher.FINAL_PRICE, AverageTextWatcher.FINAL_CNT, AverageTextWatcher.FINAL_TOTAL_PRICE
            , AverageTextWatcher.REULST_PER, AverageTextWatcher.RESULT_PRICE, AverageTextWatcher.RESULT_PRICE_PER_SHARE, AverageTextWatcher.RESULT_ADD_PER, AverageTextWatcher.RESULT_PRINCIPAL_PER
    };

    public class AverageTextWatcher implements TextWatcher {
        //key:S
        public static final String CURRENT_PRICE = "currentPrice";
        public static final String CURRENT_CNT = "currentCnt";
        public static final String ADD_PRICE = "addPrice";
        public static final String ADD_CNT = "addCnt";

        public static final String CURRENT_TOTAL_PRICE = "currentTotalPrice";
        public static final String ADD_TOTAL_PRICE = "addTotalPrice";

        public static final String FINAL_PRICE = "finalPrice";
        public static final String FINAL_CNT = "finalCnt";
        public static final String FINAL_TOTAL_PRICE = "finalTotalPrice";

        public static final String REULST_PER = "reulstPer";
        public static final String RESULT_PRICE = "resultPrice";
        public static final String RESULT_PRICE_PER_SHARE = "resultPricePerShare";
        public static final String RESULT_ADD_PER = "resultAddPer";
        public static final String RESULT_PRINCIPAL_PER = "resultPrincipalPer";
        //key:E
        private EditText targetEditText;

        private Map<String, EditText> inputMap;
        private Map<String, TextView> resultMap;

        private String prevStr;

        private IAverageCalculator mAverageCalculator;

        private AverageTextWatcher() {
        }

        public AverageTextWatcher(EditText targetEditText, Map<String, EditText> inputMap, Map<String, TextView> resultMap, IAverageCalculator calculator) {
            this.targetEditText = targetEditText;
            this.inputMap = inputMap;
            this.resultMap = resultMap;

            if (inputMap == null || inputMap.isEmpty()) {
                throw new RuntimeException("need input items");
            }
            if (resultMap == null || resultMap.isEmpty()) {
                throw new RuntimeException("need output items");
            }
            String[] inputKeys = new String[] {CURRENT_PRICE, CURRENT_CNT, ADD_PRICE, ADD_CNT};
            for (int i = 0; i < inputKeys.length; i++) {
                if (!inputMap.containsKey(inputKeys[i])) {
                    throw new RuntimeException("need input param " + inputKeys[i]);
                }
            }

            for (int i = 0; i < RESULT_KEYS.length; i++) {
                if (!resultMap.containsKey(RESULT_KEYS[i])) {
                    throw new RuntimeException("need result_keys " + RESULT_KEYS[i]);
                }
            }

            this.mAverageCalculator = calculator;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            this.prevStr = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                /**
                 * TODO: RESULT~
                 */
//                this.result1.setText("");
                return;
            }

            String num = s.toString().replaceAll("[^0-9.]","");
            if (TextUtils.isEmpty(num)) {
                num = "0";
            }
            Double d = Double.parseDouble(num);
            String newStr = decimalFormat.format(d);
            if (!TextUtils.isEmpty(prevStr) && prevStr.equalsIgnoreCase(newStr)) {
                /**
                 * TODO: RESULT~
                 */
//                this.result1.setText("");
                return;
            }
            targetEditText.removeTextChangedListener(this);
            targetEditText.setText(newStr);
            Selection.setSelection(targetEditText.getText(), newStr.length());
            targetEditText.addTextChangedListener(this);

            String currentPrice = inputMap.get(CURRENT_PRICE).getText().toString();
            String currentCnt = inputMap.get(CURRENT_CNT).getText().toString();
            String addPrice = inputMap.get(ADD_PRICE).getText().toString();
            String addCnt = inputMap.get(ADD_CNT).getText().toString();
            try {
                currentPrice = currentPrice.replaceAll("[^0-9.]","");
                currentCnt = currentCnt.replaceAll("[^0-9.]","");
                addPrice = addPrice.replaceAll("[^0-9.]","");
                addCnt = addCnt.replaceAll("[^0-9.]","");

                double _currentPrice = Double.valueOf(TextUtils.isEmpty(currentPrice) ? "0" : currentPrice);
                long _currentCnt1 = Long.valueOf(TextUtils.isEmpty(currentCnt) ? "0" : currentCnt);
                double _addPrice1 = Double.valueOf(TextUtils.isEmpty(addPrice) ? "0" : addPrice);
                long _addCnt1 = Long.valueOf(TextUtils.isEmpty(addCnt) ? "0" : addCnt);

                AverageCalculatorVO vo = this.mAverageCalculator.calculator(_currentPrice,_currentCnt1,_addPrice1,_addCnt1);

                resultMap.get(CURRENT_TOTAL_PRICE).setText(decimalFormat.format(vo.getCurrentTotalPrice()));
                resultMap.get(ADD_TOTAL_PRICE).setText(decimalFormat.format(vo.getAddTotalPrice()));

                resultMap.get(FINAL_PRICE).setText(decimalFormat.format(vo.getAvgPrice()));
                resultMap.get(FINAL_CNT).setText(decimalFormat.format(vo.getAvgCnt()));
                resultMap.get(FINAL_TOTAL_PRICE).setText(decimalFormat.format(vo.getAvgTotalPrice()));

                int textColor = Color.BLUE;
                if (_currentPrice < _addPrice1) {
                    textColor = Color.RED;
                } else if (_currentPrice == _addPrice1) {
                    textColor = getResources().getColor(R.color.font_color);
                }

                resultMap.get(REULST_PER).setText(decimalFormat.format(vo.getResultPer()) + " %");
                resultMap.get(RESULT_PRICE).setText(decimalFormat.format(vo.getResultPrice()));
                resultMap.get(RESULT_PRICE_PER_SHARE).setText(decimalFormat.format(vo.getResultPricePerShare()));
                resultMap.get(RESULT_ADD_PER).setText(decimalFormat.format(vo.getResultAddPer()) + " %");
                resultMap.get(RESULT_ADD_PER).setTextColor(Color.RED);
                if (vo.getResultPrincipalPer() > 0) {
                    resultMap.get(RESULT_PRINCIPAL_PER).setText(decimalFormat.format(vo.getResultPrincipalPer()) + " %");
                    resultMap.get(RESULT_PRINCIPAL_PER).setTextColor(Color.RED);
                } else {
                    resultMap.get(RESULT_PRINCIPAL_PER).setText("0");
                }
                String[] colorTextViews = new String[] {
                        REULST_PER, RESULT_PRICE, RESULT_PRICE_PER_SHARE};
                for (int i = 0; i < colorTextViews.length; i++) {
                    resultMap.get(colorTextViews[i]).setTextColor(textColor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ICalculator changePersentCalculator = new ICalculator() {
        @Override
        public CalculatorVO calculator(double input1, double input2) {
            CalculatorVO vo = new CalculatorVO();
            float per = (float) ((input1 - input2) / input1 * 100);
            if (per < 0) {
                vo.setText(decimalFormat.format(-per) + " % 증가");
                vo.setTextColor(Color.RED);
            } else if (per > 0) {
                vo.setText(decimalFormat.format(per) + " % 감소");
                vo.setTextColor(Color.BLUE);
            } else {
                vo.setText("0 %");
                vo.setTextColor(Color.BLACK);
            }
            return vo;
        }
    };

    ICalculator upPersentCalculator = new ICalculator() {
        @Override
        public CalculatorVO calculator(double input1, double input2) {
            CalculatorVO vo = new CalculatorVO();
            double val = input1 * (1 + (input2 / 100));

            vo.setText(decimalFormat.format(val));
            vo.setTextColor(Color.RED);
            return vo;
        }
    };

    ICalculator downPersentCalculator = new ICalculator() {
        @Override
        public CalculatorVO calculator(double input1, double input2) {
            CalculatorVO vo = new CalculatorVO();
            double val = input1 * (100 - input2) / 100;

            vo.setText(decimalFormat.format(val));
            vo.setTextColor(Color.BLUE);
            return vo;
        }
    };

    ICalculator inPersentCalculator = new ICalculator() {
        @Override
        public CalculatorVO calculator(double input1, double input2) {
            CalculatorVO vo = new CalculatorVO();
            double val = input2 / input1 * 100;

            vo.setText(decimalFormat.format(val) + " %");
            vo.setTextColor(Color.BLACK);
            return vo;
        }
    };

    ICalculator persentCalculator = new ICalculator() {
        @Override
        public CalculatorVO calculator(double input1, double input2) {
            CalculatorVO vo = new CalculatorVO();
            double val = input1 * (input2 / 100);

            vo.setText(decimalFormat.format(val));
            vo.setTextColor(Color.BLACK);
            return vo;
        }
    };


    public interface IAverageCalculator {
        AverageCalculatorVO calculator(double currentPrice, long currentCnt, double addPrice, long addCnt);
    }
    public class AverageCalculatorVO {
        private Double currentTotalPrice;
        private Double addTotalPrice;

        private Double avgTotalPrice;
        private Long avgCnt;
        private Double avgPrice;

        private Double resultPer;
        private Double resultPrice;
        private Double resultPricePerShare;
        private Double resultAddPer;
        private Double resultPrincipalPer;

        public Double getCurrentTotalPrice() {
            return currentTotalPrice;
        }

        public void setCurrentTotalPrice(Double currentTotalPrice) {
            this.currentTotalPrice = currentTotalPrice;
        }

        public Double getAddTotalPrice() {
            return addTotalPrice;
        }

        public void setAddTotalPrice(Double addTotalPrice) {
            this.addTotalPrice = addTotalPrice;
        }

        public Double getAvgTotalPrice() {
            return avgTotalPrice;
        }

        public void setAvgTotalPrice(Double avgTotalPrice) {
            this.avgTotalPrice = avgTotalPrice;
        }

        public Long getAvgCnt() {
            return avgCnt;
        }

        public void setAvgCnt(Long avgCnt) {
            this.avgCnt = avgCnt;
        }

        public Double getAvgPrice() {
            return avgPrice;
        }

        public void setAvgPrice(Double avgPrice) {
            this.avgPrice = avgPrice;
        }

        public Double getResultPer() {
            return resultPer;
        }

        public void setResultPer(Double resultPer) {
            this.resultPer = resultPer;
        }

        public Double getResultPrice() {
            return resultPrice;
        }

        public void setResultPrice(Double resultPrice) {
            this.resultPrice = resultPrice;
        }

        public Double getResultPricePerShare() {
            return resultPricePerShare;
        }

        public void setResultPricePerShare(Double resultPricePerShare) {
            this.resultPricePerShare = resultPricePerShare;
        }

        public Double getResultAddPer() {
            return resultAddPer;
        }

        public void setResultAddPer(Double resultAddPer) {
            this.resultAddPer = resultAddPer;
        }

        public Double getResultPrincipalPer() {
            return resultPrincipalPer;
        }

        public void setResultPrincipalPer(Double resultPrincipalPer) {
            this.resultPrincipalPer = resultPrincipalPer;
        }
    }

    IAverageCalculator averageCalculator = new IAverageCalculator() {
        @Override
        public AverageCalculatorVO calculator(double currentPrice, long currentCnt, double addPrice, long addCnt) {
            AverageCalculatorVO vo = new AverageCalculatorVO();

            double currentTotalPrice = currentPrice * currentCnt;
            double addTotalPrice = addPrice * addCnt;

            double avgTotalPrice = currentTotalPrice + addTotalPrice;
            long avgCnt = currentCnt + addCnt;
            double avgPrice = avgTotalPrice / avgCnt;

            double resultPer = - (currentPrice - addPrice) / currentPrice * 100;
            double resultPrice = currentTotalPrice * (resultPer / 100);
            double resultPricePerShare = addPrice - currentPrice;
            double resultAddPer = addTotalPrice / avgTotalPrice * 100;;
            double resultPrincipalPer = - (addPrice - avgPrice) / addPrice * 100;

            vo.setCurrentTotalPrice(currentTotalPrice);
            vo.setAddTotalPrice(addTotalPrice);

            vo.setAvgTotalPrice(avgTotalPrice);
            vo.setAvgCnt(avgCnt);
            vo.setAvgPrice(avgPrice);

            vo.setResultPer(resultPer);
            vo.setResultPrice(resultPrice);
            vo.setResultPricePerShare(resultPricePerShare);
            vo.setResultAddPer(resultAddPer);
            vo.setResultPrincipalPer(resultPrincipalPer);

            return vo;
        }
    };

    private CalculatorViewModel calculatorViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        calculatorViewModel =
                new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory()).get(CalculatorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_calculator, container, false);
        final EditText baek1Et1 = root.findViewById(R.id.baek1_et1);
        baek1Et1.setTag("input1");
        final EditText baek1Et2 = root.findViewById(R.id.baek1_et2);
        baek1Et2.setTag("input2");
        final TextView baek1Tv1 = root.findViewById(R.id.baek1_tv1);

        baek1Et1.addTextChangedListener(new Baek1TextWatcher(baek1Et1, baek1Et2, baek1Tv1, changePersentCalculator));
        baek1Et2.addTextChangedListener(new Baek1TextWatcher(baek1Et2, baek1Et1, baek1Tv1, changePersentCalculator));

        final EditText baek2Et1 = root.findViewById(R.id.baek2_et1);
        baek2Et1.setTag("input1");
        final EditText baek2Et2 = root.findViewById(R.id.baek2_et2);
        baek2Et2.setTag("input2");
        final TextView baek2Tv1 = root.findViewById(R.id.baek2_tv1);

        baek2Et1.addTextChangedListener(new Baek1TextWatcher(baek2Et1, baek2Et2, baek2Tv1, upPersentCalculator));
        baek2Et2.addTextChangedListener(new Baek1TextWatcher(baek2Et2, baek2Et1, baek2Tv1, upPersentCalculator));

        final EditText baek3Et1 = root.findViewById(R.id.baek3_et1);
        baek3Et1.setTag("input1");
        final EditText baek3Et2 = root.findViewById(R.id.baek3_et2);
        baek3Et2.setTag("input2");
        final TextView baek3Tv1 = root.findViewById(R.id.baek3_tv1);

        baek3Et1.addTextChangedListener(new Baek1TextWatcher(baek3Et1, baek3Et2, baek3Tv1, downPersentCalculator));
        baek3Et2.addTextChangedListener(new Baek1TextWatcher(baek3Et2, baek3Et1, baek3Tv1, downPersentCalculator));

        final EditText baek4Et1 = root.findViewById(R.id.baek4_et1);
        baek4Et1.setTag("input1");
        final EditText baek4Et2 = root.findViewById(R.id.baek4_et2);
        baek4Et2.setTag("input2");
        final TextView baek4Tv1 = root.findViewById(R.id.baek4_tv1);

        baek4Et1.addTextChangedListener(new Baek1TextWatcher(baek4Et1, baek4Et2, baek4Tv1, inPersentCalculator));
        baek4Et2.addTextChangedListener(new Baek1TextWatcher(baek4Et2, baek4Et1, baek4Tv1, inPersentCalculator));

        final EditText baek5Et1 = root.findViewById(R.id.baek5_et1);
        baek5Et1.setTag("input1");
        final EditText baek5Et2 = root.findViewById(R.id.baek5_et2);
        baek5Et2.setTag("input2");
        final TextView baek5Tv1 = root.findViewById(R.id.baek5_tv1);

        baek5Et1.addTextChangedListener(new Baek1TextWatcher(baek5Et1, baek5Et2, baek5Tv1, persentCalculator));
        baek5Et2.addTextChangedListener(new Baek1TextWatcher(baek5Et2, baek5Et1, baek5Tv1, persentCalculator));

        ///////////////////////
        final EditText currentPrice = root.findViewById(R.id.current_price);
        final EditText currentCnt = root.findViewById(R.id.current_cnt);
        final EditText addPrice = root.findViewById(R.id.add_price);
        final EditText addCnt = root.findViewById(R.id.add_cnt);

        final Map<String, EditText> inputMap = new ConcurrentHashMap<>(4);
        final Map<String, TextView> resultMap = new ConcurrentHashMap<>();

        inputMap.put(AverageTextWatcher.CURRENT_PRICE, currentPrice);
        inputMap.put(AverageTextWatcher.CURRENT_CNT, currentCnt);
        inputMap.put(AverageTextWatcher.ADD_PRICE, addPrice);
        inputMap.put(AverageTextWatcher.ADD_CNT, addCnt);

        for (int i = 0; i < RESULT_KEYS.length; i++) {
            try {
                String varName = RESULT_KEYS[i].replaceAll("[A-Z]", "_$0").toLowerCase();
                resultMap.put(RESULT_KEYS[i], root.findViewById(getResources().getIdentifier(varName, "id", getContext().getPackageName())));
            } catch (Exception e) {
                Log.e("key", RESULT_KEYS[i].toLowerCase());
                e.printStackTrace();
            }
        }

        currentPrice.addTextChangedListener(new AverageTextWatcher(currentPrice, inputMap, resultMap, averageCalculator));
        currentCnt.addTextChangedListener(new AverageTextWatcher(currentCnt, inputMap, resultMap, averageCalculator));
        addPrice.addTextChangedListener(new AverageTextWatcher(addPrice, inputMap, resultMap, averageCalculator));
        addCnt.addTextChangedListener(new AverageTextWatcher(addCnt, inputMap, resultMap, averageCalculator));

        return root;
    }
}