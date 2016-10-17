import java.util.Scanner;

/**
 * Created by Xiangxi and Yuanze on 2016/9/18.
 */
public class InputScanner {
    InputScanner() {
        dispatcher = new Dispatcher();                      // ��������ָ�����ʽ�Ķ���
        String inputString;                                 // ���������ַ���
        Scanner scanner = new Scanner(System.in);           // ����������
        do {
            inputString = scanner.nextLine();
            dispatch(inputString);
        } while(nextLoop);                                  // ѭ�����ղ�����ÿһ�����룬ֱ��dispatcher֪ͨ���ٽ���������Ϊֹ
        scanner.close();
    }
    private boolean nextLoop;
    private Dispatcher dispatcher;
    private void dispatch(String inputString) {
        dispatcher.receiveInputString(inputString);
        nextLoop = dispatcher.readyForNextLoop;
        System.out.println(dispatcher.outputString);
    }
}
