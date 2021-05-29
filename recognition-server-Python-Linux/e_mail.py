import base64
import os
import smtplib
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText


class SendMail(object):
    def __init__(self, username, passwd, recv, title, content,
                 _subtype='plain', _charset=None,
                 file=None, ssl=False,
                 email_host='smtp.qq.com', port=25, ssl_port=465):
        '''
        :param username: 用户名
        :param passwd: 密码
        :param recv: 收件人，多个要传list ['a@qq.com','b@qq.com]
        :param title: 邮件标题
        :param content: 邮件正文
        :param file: 附件路径，如果不在当前目录下，要写绝对路径，默认没有附件
        :param ssl: 是否安全链接，默认为普通
        :param email_host: smtp服务器地址，默认为163服务器
        :param port: 非安全链接端口，默认为25
        :param ssl_port: 安全链接端口，默认为465
        '''
        self.username = username  # 用户名
        self.passwd = passwd  # 密码
        self.recv = recv  # 收件人，多个要传list ['a@qq.com','b@qq.com]
        self.title = title  # 邮件标题
        self.content = content  # 邮件正文
        self.file = file  # 附件路径，如果不在当前目录下，要写绝对路径
        self.email_host = email_host  # smtp服务器地址
        self.port = port  # 普通端口
        self.ssl = ssl  # 是否安全链接
        self.ssl_port = ssl_port  # 安全链接端口
        self._subtype = _subtype    # 正文类型
        self._charset = _charset    # 编码格式

    def send_mail(self):
        msg = MIMEMultipart()
        # 发送内容的对象
        if self.file:  # 处理附件的
            file_name = os.path.split(self.file)[-1]  # 只取文件名，不取路径
            try:
                f = open(self.file, 'rb').read()
            except Exception as e:
                raise Exception('附件打不开！！！！')
            else:
                att = MIMEText(f, "base64", "utf-8")
                att["Content-Type"] = 'application/octet-stream'
                # base64.b64encode(file_name.encode()).decode()
                new_file_name = '=?utf-8?b?' + base64.b64encode(file_name.encode()).decode() + '?='
                # 这里是处理文件名为中文名的，必须这么写
                att["Content-Disposition"] = 'attachment; filename="%s"' % (new_file_name)
                msg.attach(att)
        msg.attach(MIMEText(self.content, _subtype= self._subtype, _charset=self._charset))  # 邮件正文的内容
        msg['Subject'] = self.title  # 邮件主题
        msg['From'] = self.username  # 发送者账号
        msg['To'] = ','.join(self.recv)  # 接收者账号列表

        # html内嵌图片
        # fp = open(r'recognition_img/recognize.jpeg', 'rb')
        # msgImage = MIMEImage(fp.read())
        # fp.close()
        # msgImage.add_header('Content-ID', '<image1>')
        # msg.attach(msgImage)

        # # 定义一个字符串，内容就是HTML代码
        # html_msg = \
        #     """
        #     <!DOCTYPE html>
        #     <html lang="en">
        #     <head>
        #         <meta charset="UTF-8">
        #         <title>Title</title>
        #     </head>
        #     <body>
        #     <h1>警报! 氧气不足！</h1>
        #     <h2>'警报! 氧气不足！请尽快更换新的氧气罐！</h2>
        #     <hr>
        #     <img src="cid:image1" height="640" width="480">
        #     </body>
        #     </html>
        #     """

        if self.ssl:
            self.smtp = smtplib.SMTP_SSL(self.email_host, port=self.ssl_port)
        else:
            self.smtp = smtplib.SMTP(self.email_host, port=self.port)
        # 发送邮件服务器的对象
        self.smtp.login(self.username, self.passwd)
        try:
            self.smtp.sendmail(self.username, self.recv, msg.as_string())
            pass
        except Exception as e:
            print('出错了。。', e)
        else:
            print('发送成功！')
        self.smtp.quit()


if __name__ == '__main__':



    m = SendMail(
        username='lizhixingss@qq.com',
        passwd='qahpxjupvituddje',
        recv=['1632971113@qq.com', "2504989046@qq.com"],
        title='警报! 氧气不足！',
        content='警报! 氧气不足！请尽快更换新的氧气罐！',
        # _subtype="html",
        # _charset="utf-8",
        # file=r'recognition_img/recognize.jpeg',
        ssl=True,
    )
    m.send_mail()
