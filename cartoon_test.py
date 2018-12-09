def method3(img):
    input = cv2.imread(img)

    #we produce colors with mean shift filtering
    shifted = cv2.pyrMeanShiftFiltering(input, 15, 20)
    cv2.imwrite("mean_shift.png", shifted)
    #Process for finding edges
    gray = cv2.cvtColor(input, cv2.COLOR_BGRA2GRAY)
    edges = cv2.blur(gray, (4, 4)) # this blur gets rid of some noise
    edges = cv2.Canny(edges, 50, 150, apertureSize=3) # this is the edge detection

    # Thicken the edges
    kernel = np.ones((3,3), dtype=np.float) / 12.0
    edges = cv2.filter2D(edges, 0, kernel)
    edges = cv2.threshold(edges, 50, 255, 0)[1]
    edges = cv2.cvtColor(edges, cv2.COLOR_GRAY2BGR)
    cv2.imwrite("mean_shift_edges.png", edges)
    #output is created by subtracting the edges from the color
    out = cv2.subtract(shifted, edges)

    cv2.imwrite("out_test.png", out)
    return out

def method4(img):
    # downsample image
    img_color = imread(img)
    img_color = cv2.pyrDown(img_color)
    # repeatedly apply small bilateral filter instead of applying
    # one large filter
    i = 0
    while i < 150:
        img_color = cv2.bilateralFilter(img_color, 9, 9, 7)
        i+=1

    # upsample image to original size
    img_color = cv2.pyrUp(img_color)
    # convert to grayscale
    img_gray = cv2.cvtColor(img_color, cv2.COLOR_RGB2GRAY)
    #apply bilateral filter
    i = 0
    while i < 150:
        img_gray_blur = cv2.bilateralFilter(img_gray, 9, 9, 7)
        i+=1
    #detect/enhance edges
    img_edge = cv2.adaptiveThreshold(img_gray_blur, 252,
                                     cv2.ADAPTIVE_THRESH_GAUSSIAN_C,
                                     cv2.THRESH_BINARY, 9, 5)
    #convert back to color so that it can be bit-ANDed with color image
    img_edge = cv2.cvtColor(img_edge, cv2.COLOR_GRAY2RGB)
    #make sure img_color and img_edge are the same size, otherwise bitwise_and will not work
    height = min(len(img_color), len(img_edge))
    width = min(len(img_color[0]), len(img_edge[0]))
    img_color = img_color[0:height, 0:width]
    cv2.imwrite("color2.jpg", img_color)
    img_edge = img_edge[0:height, 0:width]
    img_edge = np.multiply(1, img_edge)
    img_edge = cv2.fastNlMeansDenoisingColored(img_edge, None, 50, 10, 9, 31)
    cv2.imwrite("edge2.jpg", img_edge)

    c = cv2.bitwise_and(img_color, img_edge)

    cv2.imwrite("out_cartoon_1.jpg", c)
    return c
