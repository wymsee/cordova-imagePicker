//
//  AssetCell.m
//
//  Created by ELC on 2/15/11.
//  Copyright 2011 ELC Technologies. All rights reserved.
//

#import "ELCAssetCell.h"
#import "ELCAsset.h"

@interface ELCAssetCell ()

@property (nonatomic, strong) NSArray *rowAssets;
@property (nonatomic, strong) NSMutableArray *imageViewArray;
@property (nonatomic, strong) NSMutableArray *checkIconArray;
@property (nonatomic, strong) NSMutableArray *circleIconArray;

@end

@implementation ELCAssetCell

//Using auto synthesizers

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:UITableViewCellStyleDefault reuseIdentifier:reuseIdentifier];
	if (self) {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(cellTapped:)];
        [self addGestureRecognizer:tapRecognizer];
        
        NSMutableArray *mutableArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.imageViewArray = mutableArray;
        
        NSMutableArray *checkIconArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.checkIconArray = checkIconArray;
        
        NSMutableArray *circleIconArray = [[NSMutableArray alloc] initWithCapacity:4];
        self.circleIconArray = circleIconArray;
	}
	return self;
}

- (void)setAssets:(NSArray *)assets
{
    self.rowAssets = assets;
	for (UIImageView *view in _imageViewArray) {
        [view removeFromSuperview];
	}

    for (UILabel *view in _checkIconArray) {
        [view removeFromSuperview];
    }
    
    for (UILabel *view in _circleIconArray) {
        [view removeFromSuperview];
    }

    UIFont *donkeyFont = [UIFont fontWithName:@"DonkeyFont" size:25];
    UILabel *checkLabel = nil;
    UILabel *circleLabel = nil;
    CGSize overlaySize;
    for (int i = 0; i < [_rowAssets count]; ++i) {

        ELCAsset *asset = [_rowAssets objectAtIndex:i];

        if (i < [_imageViewArray count]) {
            UIImageView *imageView = [_imageViewArray objectAtIndex:i];
            imageView.image = [UIImage imageWithCGImage:asset.asset.thumbnail];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        } else {
            UIImageView *imageView = [[UIImageView alloc] initWithImage:[UIImage imageWithCGImage:asset.asset.thumbnail]];
            [_imageViewArray addObject:imageView];
            overlaySize = CGSizeMake(CGRectGetWidth(imageView.frame), CGRectGetHeight(imageView.frame));
        }

        assert([_checkIconArray count] == [_circleIconArray count]);
        if (i < [_checkIconArray count]) {
            UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
            UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
        } else {
            checkLabel = [[UILabel alloc] init];
            checkLabel.textColor = [UIColor whiteColor];
            checkLabel.font = donkeyFont;
            checkLabel.textAlignment = NSTextAlignmentLeft;
            [_checkIconArray addObject:checkLabel];
            
            circleLabel = [[UILabel alloc] init];
            circleLabel.textColor = [UIColor whiteColor];
            circleLabel.font = donkeyFont;
            circleLabel.textAlignment = NSTextAlignmentLeft;
            [_circleIconArray addObject:circleLabel];
            
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
        }
    }
}

- (void)cellTapped:(UITapGestureRecognizer *)tapRecognizer
{
    CGPoint point = [tapRecognizer locationInView:self];
    CGFloat totalWidth = self.rowAssets.count * 75 + (self.rowAssets.count - 1) * 4;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, 2, 75, 75);
	
    assert([_checkIconArray count] == [_circleIconArray count]);
    for (int i = 0; i < [_rowAssets count]; ++i) {

        if (CGRectContainsPoint(frame, point)) {
            ELCAsset *asset = [_rowAssets objectAtIndex:i];
            asset.selected = !asset.selected;
            UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
            UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
            [self selectThumbnail:checkLabel circleLabel:circleLabel selected:asset.selected];
            break;
        }
        frame.origin.x = frame.origin.x + frame.size.width + 4;
    }
}

- (void)layoutSubviews
{    
    CGFloat totalWidth = self.rowAssets.count * 75 + (self.rowAssets.count - 1) * 4;
    CGFloat startX = (self.bounds.size.width - totalWidth) / 2;
    
	CGRect frame = CGRectMake(startX, 2, 75, 75);
	
	for (int i = 0; i < [_rowAssets count]; ++i) {
		UIImageView *imageView = [_imageViewArray objectAtIndex:i];
		[imageView setFrame:frame];
		[self addSubview:imageView];
        
        UILabel *checkLabel = [_checkIconArray objectAtIndex:i];
        UILabel *circleLabel = [_circleIconArray objectAtIndex:i];
        CGRect iconFrame = CGRectMake(frame.origin.x + 5,
                                         frame.origin.y + 5,
                                         frame.size.width,
                                         frame.size.height / 3);
        [checkLabel setFrame:iconFrame];
        [circleLabel setFrame:iconFrame];
        [self addSubview:circleLabel];
        
        [self addSubview:checkLabel];
        
        
		
		frame.origin.x = frame.origin.x + frame.size.width + 4;
	}
}

- (void)selectThumbnail:(UILabel*)checkLabel circleLabel:(UILabel*)circleLabel selected:(bool)selected {
    checkLabel.text = selected ? @"\uE060" : @"\uE061";
    checkLabel.textColor = selected ? [UIColor colorWithRed:22.0f / 255.0f
                                                      green:157 / 255.0f
                                                       blue:217.0f / 255.0f
                                                      alpha:1.0] : [UIColor whiteColor];
    
    circleLabel.text = @"\uE06E";
    circleLabel.textColor = [UIColor whiteColor];
    circleLabel.alpha = selected ? 1.0f : 0.0f;
}


@end
